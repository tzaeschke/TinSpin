/*
 * Copyright 2016 Tilmann Z�schke
 * 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zoodb.index.quadtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.zoodb.index.quadtree.QuadTreeKD.QStats;

/**
 * Node class for the quadtree.
 * 
 * @author ztilmann
 *
 * @param <T>
 */
public class QRNode<T> {

	private static final int OVERLAP_WITH_CENTER = -1;
	private double[] min;
	private double[] max;
	//null indicates that we have sub-nopde i.o. values
	private ArrayList<QREntry<T>> values;
	private QRNode<T>[] subs;
	
	QRNode(double[] min, double[] max) {
		this.min = min;
		this.max = max;
		this.values = new ArrayList<>(); 
	}

	@SuppressWarnings("unchecked")
	QRNode(double[] min, double[] max, QRNode<T> subNode, int subNodePos) {
		this.min = min;
		this.max = max;
		this.values = null;
		this.subs = new QRNode[1 << min.length];
		subs[subNodePos] = subNode;
	}

	@SuppressWarnings("unchecked")
	QRNode<T> tryPut(QREntry<T> e, int maxNodeSize, boolean enforceLeaf) {
		if (QuadTreeKD.DEBUG && !e.enclosedByXX(min, max)) {
			throw new IllegalStateException("e=" + e + 
					" min/max=" + Arrays.toString(min) + Arrays.toString(max));
		}
		
		//traverse subs?
		int pos = calcSubPositionR(e.getPointL(), e.getPointU());
		if (subs != null && pos != OVERLAP_WITH_CENTER) {
			return getOrCreateSubR(pos);
		}
		
		//add if:
		//a) we have space
		//b) we have maxDepth
		//c) elements are equal (work only for n=1, avoids splitting
		//   in cases where splitting won't help. For n>1 the
		//   local limit is (temporarily) violated.
		//d) We already have subs, which means that the local entries all 
		//   overlap with the centerpoint
		if (values == null) {
			values = new ArrayList<>();
		}
		if (values.size() < maxNodeSize || enforceLeaf || 
				e.isExact(values.get(0)) || subs != null) {
			values.add(e);
			return null;
		}
		
		//split
		ArrayList<QREntry<T>> vals = values;
		values = null;
		subs = new QRNode[1 << min.length];
		for (int i = 0; i < vals.size(); i++) {
			QREntry<T> e2 = vals.get(i); 
			int pos2 = calcSubPositionR(e2.getPointL(), e2.getPointU());
			if (pos2 == OVERLAP_WITH_CENTER) {
				if (values == null) {
					values = new ArrayList<>();
				}
				values.add(e2);
				continue;
			}
			QRNode<T> sub = getOrCreateSubR(pos2);
			while (sub != null) {
				//This may recurse if all entries fall 
				//into the same subnode
				sub = (QRNode<T>) sub.tryPut(e2, maxNodeSize, false);
			}
		}
		if (pos == OVERLAP_WITH_CENTER) {
			if (values == null) {
				values = new ArrayList<>();
			}
			values.add(e);
			return null;
		}
		return getOrCreateSubR(pos);
	}

	private QRNode<T> getOrCreateSubR(int pos) {
		QRNode<T> n = subs[pos];
		if (n == null) {
			n = createSubForEntry(pos);
			subs[pos] = n;
		}
		return n;
	}
	
	private QRNode<T> createSubForEntry(int subNodePos) {
		double[] minSub = new double[min.length];
		double[] maxSub = new double[max.length];
		double len = (max[0]-min[0])/2;
		int mask = 1<<min.length;
		for (int d = 0; d < min.length; d++) {
			mask >>= 1;
			if ((subNodePos & mask) > 0) {
				minSub[d] = min[d]+len;
				maxSub[d] = max[d];
			} else {
				minSub[d] = min[d];
				maxSub[d] = max[d]-len; 
			}
		}
		return new QRNode<>(minSub, maxSub);		
	}
	
	/**
	 * The subnode position has reverse ordering of the point's
	 * dimension ordering. Dimension 0 of a point is the highest
	 * ordered bit in the position.
	 * @param p point
	 * @return subnode position
	 */
	private int calcSubPositionR(double[] pMin, double[] pMax) {
		int subNodePos = 0;
		double len = (max[0]-min[0])/2;
		for (int d = 0; d < min.length; d++) {
			subNodePos <<= 1;
			if (pMin[d] >= min[d]+len) {
				subNodePos |= 1;
			} else if (pMax[d] >= min[d]+len) {
				//overlap with center point
				return OVERLAP_WITH_CENTER;
			}
		}
		return subNodePos;
	}

	QREntry<T> remove(QRNode<T> parent, double[] keyL, double[] keyU, int maxNodeSize) {
		if (subs != null) {
			int pos = calcSubPositionR(keyL, keyU);
			if (pos != OVERLAP_WITH_CENTER) {
				QRNode<T> sub = subs[pos];
				if (sub != null) {
					return sub.remove(this, keyL, keyU, maxNodeSize);
				}
				return null;
			}
		}
		
		//now check local data
		if (values == null) {
			return null;
		}
		for (int i = 0; i < values.size(); i++) {
			QREntry<T> e = values.get(i);
			if (QUtil.isRectEqual(e, keyL, keyU)) {
				values.remove(i);
				//TODO provide threshold for re-insert
				//i.e. do not always merge.
				if (parent != null) {
					parent.checkAndMergeLeafNodes(maxNodeSize);
				}
				return e;
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	QREntry<T> update(QRNode<T> parent, double[] keyOldL, double[] keyOldU, 
			double[] keyNewL, double[] keyNewU, int maxNodeSize,
			boolean[] requiresReinsert, int currentDepth, int maxDepth) {
		if (subs != null) {
			int pos = calcSubPositionR(keyOldL, keyOldU);
			if (pos != OVERLAP_WITH_CENTER) {
				QRNode<T> sub = subs[pos];
				if (sub == null) {
					return null;
				}
				QREntry<T> ret = sub.update(this, keyOldL, keyOldU, keyNewL, keyNewU, 
						maxNodeSize, requiresReinsert, currentDepth+1, maxDepth);
				if (ret != null && requiresReinsert[0] && 
						QUtil.isRectEnclosed(ret.getPointL(), ret.getPointU(), min, max)) {
					requiresReinsert[0] = false;
					Object r = this;
					while (r instanceof QRNode) {
						r = ((QRNode<T>)r).tryPut(ret, maxNodeSize, currentDepth++ > maxDepth);
					}
				}
				return ret;
			}
		}
		
		//now check local data
		if (values == null) {
			return null;
		}
		for (int i = 0; i < values.size(); i++) {
			QREntry<T> e = values.get(i);
			if (QUtil.isRectEqual(e, keyOldL, keyOldU)) {
				values.remove(i);
				e.setKey(keyNewL, keyNewU);
				if (QUtil.isRectEnclosed(keyNewL, keyNewU, min, max)) {
					requiresReinsert[0] = false;
					int pos = calcSubPositionR(keyNewL, keyNewU);
					if (pos == OVERLAP_WITH_CENTER) {
						//reinsert locally;
						values.add(e);
					} else {
						Object r = this;
						while (r instanceof QRNode) {
							r = ((QRNode<T>)r).tryPut(e, maxNodeSize, currentDepth++ > maxDepth);
						}
					}
				} else {
					requiresReinsert[0] = true;
					//TODO provide threshold for re-insert
					//i.e. do not always merge.
					if (parent != null) {
						parent.checkAndMergeLeafNodes(maxNodeSize);
					}
				}
				return e;
			}
		}
		requiresReinsert[0] = false;
		return null;
	}

	private void checkAndMergeLeafNodes(int maxNodeSize) {
		//check
		int nTotal = 0;
		if (values != null) {
			nTotal += values.size();
		}
		for (int i = 0; i < subs.length; i++) {
			if (subs[i] != null) {
				if (subs[i].subs != null) {
					//can't merge directory nodes.
					return; 
				}
				if (subs[i].values != null) {
					nTotal += subs[i].values.size();
				}					
				if (nTotal > maxNodeSize) {
					//too many children
					return;
				}
			}
		}
		
		//okay, let's merge
		if (values == null) {
			values = new ArrayList<>();
		}
		for (int i = 0; i < subs.length; i++) {
			if (subs[i] != null) {
				values.addAll(subs[i].values);
			}
		}
		subs = null;
	}

	double getSideLength() {
		return max[0]-min[0];
	}

	double[] getMin() {
		return min;
	}

	double[] getMax() {
		return max;
	}

	QREntry<T> getExact(double[] keyL, double[] keyU) {
		if (subs != null) {
			int pos = calcSubPositionR(keyL, keyU);
			if (pos != OVERLAP_WITH_CENTER) {
				QRNode<T> sub = subs[pos];
				if (sub != null) {
					return sub.getExact(keyL, keyU);
				}
				return null;
			}
		}
		
		if (values == null) {
			return null;
		}
		
		for (int i = 0; i < values.size(); i++) {
			QREntry<T> e = values.get(i);
			if (QUtil.isRectEqual(e, keyL, keyU)) {
				return e;
			}
		}
		return null;
	}

	ArrayList<QREntry<T>> getEntries() {
		return values;
	}

	Iterator<?> getChildIterator() {
		if (subs == null) {
			return values.iterator();
		}
		return new ArrayIterator<>(subs, values != null ? values.toArray() : null);
	}

	private static class ArrayIterator<E> implements Iterator<E> {

		private E[] data;
		private E[] data2;
		private int pos;
		
		ArrayIterator(E[] data1, E[] data2) {
			this.data = data1;
			this.data2 = data2;
			this.pos = 0;
			findNext();
		}
		
		private void findNext() {
			while (pos < data.length && data[pos] == null) {
				pos++;
			}
			if (pos >= data.length && data2 != null) {
				data = data2;
				data2 = null;
				pos = 0;
				findNext();
			}
		}
		
		@Override
		public boolean hasNext() {
			return pos < data.length;
		}

		@Override
		public E next() {
			E ret = data[pos++];
			findNext();
			return ret;
		}
		
	}
	
	@Override
	public String toString() {
		return "min/max=" + Arrays.toString(min) + Arrays.toString(max) + 
				" " + System.identityHashCode(this);
	}

	void checkNode(QStats s, QRNode<T> parent, int depth) {
		if (depth > s.maxDepth) {
			s.maxDepth = depth;
		}
		s.nNodes++;
		
		if (parent != null) {
			if (!QUtil.isRectEnclosed(min, max, parent.min, parent.max)) {
				throw new IllegalStateException();
			}
		}
		if (values != null) {
			for (int i = 0; i < values.size(); i++) {
				QREntry<T> e = values.get(i);
				if (!QUtil.isRectEnclosed(e.getPointL(), e.getPointU(), min, max)) {
					throw new IllegalStateException();
				}
				//TODO check that they overlap with the centerpoint or that subs==null
			}
		} 
		if (subs != null) {
			for (int i = 0; i < subs.length; i++) {
				QRNode<T> n = subs[i];
				//TODO check pos
				if (n != null) {
					n.checkNode(s, this, depth+1);
				}
			}
		}
	}

	QRNode<T>[] getChildNodes() {
		return subs;
	}
}
