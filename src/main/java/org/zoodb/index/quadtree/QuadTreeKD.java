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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A simple MX-quadtree implementation with configurable maximum depth, maximum nodes size, and
 * (if desired) automatic guessing of root rectangle. 
 * 
 * @author ztilmann
 *
 * @param <T>
 */
public class QuadTreeKD<T> {

	private static final int MAX_DEPTH = 50;
	
	private static final String NL = System.lineSeparator();

	public static final boolean DEBUG = false;
	private static final int DEFAULT_MAX_NODE_SIZE = 10;
	
	private final int dims;
	private final int maxNodeSize;
	private QNode<T> root = null;
	private int size = 0; 
	
	private QuadTreeKD(int dims, int maxNodeSize) {
		if (dims > 6) {
			throw new UnsupportedOperationException();
		}
		if (DEBUG) {
			System.err.println("Warning: DEBUG enabled");
		}
		this.dims = dims;
		this.maxNodeSize = maxNodeSize;
	}

	public static <T> QuadTreeKD<T> create(int dims) {
		return new QuadTreeKD<>(dims, DEFAULT_MAX_NODE_SIZE);
	}
	
	public static <T> QuadTreeKD<T> create(int dims, int maxNodeSize) {
		return new QuadTreeKD<>(dims, maxNodeSize);
	}
	
	public static <T> QuadTreeKD<T> create(int dims, int maxNodeSize, 
			double[] min, double[] max) {
		QuadTreeKD<T> t = new QuadTreeKD<>(dims, maxNodeSize);
		t.root = new QNode<>( 
				Arrays.copyOf(min, min.length), 
				Arrays.copyOf(max, max.length));
		return t;
	}
	
	/**
	 * Insert a key-value pair.
	 * @param key the key
	 * @param value the value
	 */
	@SuppressWarnings("unchecked")
	public void put(double[] key, T value) {
		size++;
		QEntry<T> e = new QEntry<>(key, value);
		if (root == null) {
			initializeRoot(key);
		}
		ensureCoverage(e);
		Object r = root;
		int depth = 0;
		while (r instanceof QNode) {
			r = ((QNode<T>)r).tryPut(e, maxNodeSize, depth++>MAX_DEPTH);
		}
	}
	
	private void initializeRoot(double[] key) {
		double lo = Double.MAX_VALUE;
		double hi = -Double.MAX_VALUE;
		for (int d = 0; d < dims; d++) {
			lo = lo > key[d] ? key[d] : lo;
			hi = hi < key[d] ? key[d] : hi;
		}
		if (lo == 0 && hi == 0) {
			hi = 1.0; 
		}
		double maxDistOrigin = Math.abs(hi) > Math.abs(lo) ? hi : lo;
		maxDistOrigin = Math.abs(maxDistOrigin);
		//no we use (0,0)/(+-maxDistOrigin*2,+-maxDistOrigin*2) as root.
		double[] min = new double[dims];
		double[] max = new double[dims];
		for (int d = 0; d < dims; d++) {
			min[d] = key[d] > 0 ? 0 : -(maxDistOrigin*2);
			max[d] = key[d] < 0 ? 0 : (maxDistOrigin*2);
		}			
		root = new QNode<T>(min, max);
	}
	
	/**
	 * Check whether a given key exists.
	 * @param key the key to check
	 * @return true iff the key exists
	 */
	public boolean containsExact(double[] key) {
		if (root == null) {
			return false;
		}
		return root.getExact(key) != null;
	}
	
	/**
	 * Get the value associates with the key.
	 * @param key the key to look up
	 * @return the value for the key or 'null' if the key was not found
	 */
	public T getExact(double[] key) {
		if (root == null) {
			return null;
		}
		QEntry<T> e = root.getExact(key);
		return e == null ? null : e.getValue();
	}
	
	/**
	 * Remove a key.
	 * @param key key to remove
	 * @return the value associated with the key or 'null' if the key was not found
	 */
	public T removeExact(double[] key) {
		if (root == null) {
			return null;
		}
		QEntry<T> e = root.remove(null, key, maxNodeSize);
		if (e == null) {
			return null;
		}
		size--;
		return e.getValue();
	}

	/**
	 * Reinsert the key.
	 * @param oldKey old key
	 * @param newKey new key
	 * @return the value associated with the key or 'null' if the key was not found.
	 */
	@SuppressWarnings("unchecked")
	public T update(double[] oldKey, double[] newKey) {
		if (root == null) {
			return null;
		}
		boolean[] requiresReinsert = new boolean[]{false};
		QEntry<T> e = root.update(null, oldKey, newKey, maxNodeSize, requiresReinsert, 
				0, MAX_DEPTH);
		if (e == null) {
			//not found
			return null;
		}
		if (requiresReinsert[0]) {
			//does not fit in root node...
			ensureCoverage(e);
			Object r = root;
			int depth = 0;
			while (r instanceof QNode) {
				r = ((QNode<T>)r).tryPut(e, maxNodeSize, depth++>MAX_DEPTH);
			}
		}
		return e.getValue();
	}
	
	/**
	 * Ensure that the tree covers the entry.
	 * @param e Entry to cover.
	 */
	private void ensureCoverage(QEntry<T> e) {
		double[] p = e.getPoint();
		while(!e.enclosedBy(root.getMin(), root.getMax())) {
			double len = root.getSideLength();
			double[] min = root.getMin();
			double[] max = root.getMax();
			double[] min2 = new double[min.length];
			double[] max2 = new double[max.length];
			int subNodePos = 0;
			for (int d = 0; d < min.length; d++) {
				subNodePos <<= 1;
				if (p[d] < min[d]) {
					min2[d] = min[d]-len;
					max2[d] = max[d];
					//root will end up in upper quadrant in this 
					//dimension
					subNodePos |= 1;
				} else {
					//extend upwards, even if extension unnecessary for this dimension.
					min2[d] = min[d];
					max2[d] = max[d]+len; 
				}
			}
			if (QuadTreeKD.DEBUG && !QUtil.isRectEnclosed(min, max, min2, max2)) {
				throw new IllegalStateException("e=" + Arrays.toString(e.getPoint()) + 
						" min/max=" + Arrays.toString(min) + Arrays.toString(max));
			}
			root = new QNode<>(min2, max2, root, subNodePos);
		}
	}
	
	/**
	 * Get the number of key-value pairs in the tree.
	 * @return the size
	 */
	public int size() {
		return size;
	}

	/**
	 * Removes all elements from the tree.
	 */
	public void clear() {
		size = 0;
		root = null;
	}

	/**
	 * Query the tree, returning all points in the axis-aligned rectangle between 'min' and 'max'.
	 * @param min lower left corner of query
	 * @param max upper right corner of query
	 * @return all entries in the rectangle
	 */
	public QIterator<T> query(double[] min, double[] max) {
		return new QIterator<>(this, min, max);
	}

	/**
	 * Resettable query iterator.
	 *
	 * @param <T>
	 */
	public static class QIterator<T> implements Iterator<QEntry<T>> {

		private final QuadTreeKD<T> tree;
		private ArrayDeque<Iterator<?>> stack;
		private QEntry<T> next = null;
		private double[] min;
		private double[] max;
		
		QIterator(QuadTreeKD<T> tree, double[] min, double[] max) {
			this.stack = new ArrayDeque<>();
			this.tree = tree;
			reset(min, max);
		}
		
		@SuppressWarnings("unchecked")
		private void findNext() {
			while(!stack.isEmpty()) {
				Iterator<?> it = stack.peek();
				while (it.hasNext()) {
					Object o = it.next();
					if (o instanceof QNode) {
						QNode<T> node = (QNode<T>)o;
						if (QUtil.overlap(min, max, node.getMin(), node.getMax())) {
							it = node.getChildIterator();
							stack.push(it);
						}
						continue;
					}
					QEntry<T> e = (QEntry<T>) o;
					if (e.enclosedBy(min, max)) {
						next = e;
						return;
					}
				}
				stack.pop();
			}
			next = null;
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public QEntry<T> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			QEntry<T> ret = next;
			findNext();
			return ret;
		}

		/**
		 * Reset the iterator. This iterator can be reused in order to reduce load on the
		 * garbage collector.
		 * @param min lower left corner of query
		 * @param max upper right corner of query
		 */
		public void reset(double[] min, double[] max) {
			stack.clear();
			this.min = min;
			this.max = max;
			next = null;
			if (tree.root != null) {
				stack.push(tree.root.getChildIterator());
				findNext();
			}
		}
	}
	
	public List<QEntryDist<T>> knnSearch(double[] center, int k) {
		if (root == null) {
    		return Collections.emptyList();
		}
        Comparator<QEntry<T>> comp =  
        		(QEntry<T> point1, QEntry<T> point2) -> {
        			double deltaDist = 
        					QUtil.distance(center, point1.getPoint()) - 
        					QUtil.distance(center, point2.getPoint());
        			return deltaDist < 0 ? -1 : (deltaDist > 0 ? 1 : 0);
        		};
        double distEstimate = distanceEstimate(root, center, k, comp);
    	ArrayList<QEntryDist<T>> candidates = new ArrayList<>();
    	while (candidates.size() < k) {
    		candidates.clear();
    		knnSearchNew(root, center, k, distEstimate, candidates);
    		distEstimate *= 2;
    	}
    	return candidates;
    }

    @SuppressWarnings("unchecked")
	private double distanceEstimate(QNode<T> node, double[] point, int k,
    		Comparator<QEntry<T>> comp) {
    	if (node.isLeaf()) {
    		//This is a lead that would contain the point.
    		int n = node.getEntries().size();
    		QEntry<T>[] data = node.getEntries().toArray(new QEntry[n]);
    		Arrays.sort(data, comp);
    		int pos = n < k ? n : k;
    		double dist = QUtil.distance(point, data[pos-1].getPoint());
    		if (n < k) {
    			//scale search dist with dimensions.
    			dist = dist * Math.pow(k/(double)n, 1/(double)dims);
    		}
    		return dist;
    	} else {
    		QNode<T>[] nodes = node.getChildNodes(); 
    		for (int i = 0; i < nodes.length; i++) {
    			if (nodes[i] != null && 
    					QUtil.isPointEnclosed(point, nodes[i].getMin(), nodes[i].getMax())) {
    				return distanceEstimate(nodes[i], point, k, comp);
    			}
    		}
    		//okay, this directory node contains the point, but none of the leaves does.
    		//We just return the size of this node, because all it's leaf nodes should
    		//contain more than enough candidate in proximity of 'point'.
    		double distMin = QUtil.distance(point, node.getMin()); 
    		double distMax = QUtil.distance(point, node.getMax());
    		//Return distance to farthest corner as approximation
    		return distMin < distMax ? distMax : distMin;
    	}
    }
    
    private void knnSearchNew(QNode<T> start, double[] center, int k, double range,
    		ArrayList<QEntryDist<T>> candidates) {
        double[] mbrPointsMin = new double[dims];
        double[] mbrPointsMax = new double[dims];
        for (int i = 0; i < dims; i++) {
            mbrPointsMin[i] = center[i] - range;
            mbrPointsMax[i] = center[i] + range;
        }
        rangeSearchKNN(start, center, mbrPointsMin, mbrPointsMax, candidates, k, range);
    }

    private double rangeSearchKNN(QNode<T> node, double[] center, double[] min, double[] max, 
    		ArrayList<QEntryDist<T>> candidates, int k, double maxRange) {
		if (node.isLeaf()) {
    		ArrayList<QEntry<T>> points = node.getEntries();
    		for (int i = 0; i < points.size(); i++) {
    			QEntry<T> p = points.get(i);
   				double dist = QUtil.distance(center, p.getPoint());
   				if (dist < maxRange) {
    				candidates.add(new QEntryDist<>(p, dist));
  				}
    		}
    		maxRange = adjustRegionKNN(min, max, center, candidates, k, maxRange);
    	} else {
    		QNode<T>[] nodes = node.getChildNodes(); 
    		for (int i = 0; i < nodes.length; i++) {
    			QNode<T> sub = nodes[i];
    			if (sub != null && QUtil.overlap(min, max, sub.getMin(), sub.getMax())) {
    				maxRange = rangeSearchKNN(sub, center, min, max, candidates, k, maxRange);
    				//we set maxRange simply to the latest returned value.
    			}
    		}
    	}
    	return maxRange;
    }

    private double adjustRegionKNN(double[] min, double[] max, double[] center, 
    		ArrayList<QEntryDist<T>> candidates, int k, double maxRange) {
        if (candidates.size() < k) {
        	//wait for more candidates
        	return maxRange;
        }

        //use stored distances instead of recalcualting them
        candidates.sort(QEntryDist.COMP);
        while (candidates.size() > k) {
        	candidates.remove(candidates.size()-1);
        }
        
        double range = candidates.get(candidates.size()-1).getDistance();
        for (int i = 0; i < dims; i++) {
            min[i] = center[i] - range;
            max[i] = center[i] + range;
        }
        return range;
	}
	
    /**
	 * Returns a printable list of the tree.
	 * @return the tree as String
	 */
	public String toStringTree() {
		StringBuilder sb = new StringBuilder();
		if (root == null) {
			sb.append("empty tree");
		} else {
			toStringTree(sb, root, 0, 0);
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	private void toStringTree(StringBuilder sb, QNode<T> node, 
			int depth, int posInParent) {
		Iterator<?> it = node.getChildIterator();
		String prefix = "";
		for (int i = 0; i < depth; i++) {
			prefix += ".";
		}
		sb.append(prefix + posInParent + " d=" + depth);
		sb.append(" " + Arrays.toString(node.getMin()));
		sb.append("/" + Arrays.toString(node.getMax())+ NL);
		prefix += " ";
		int pos = 0;
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof QNode) {
				QNode<T> sub = (QNode<T>) o;
				toStringTree(sb, sub, depth+1, pos);
			} else if (o instanceof QEntry) {
				QEntry<T> e = (QEntry<T>) o;
				sb.append(prefix + Arrays.toString(e.getPoint()));
				sb.append(" v=" + e.getValue() + NL);
			}
			pos++;
		}
	}
	
	@Override
	public String toString() {
		return "QuadTreeKD;maxNodeSize=" + maxNodeSize + 
				";maxDepth=" + MAX_DEPTH + 
				";DEBUG=" + DEBUG + 
				";min/max=" + (root==null ? "null" : 
					(Arrays.toString(root.getMin()) + "/" + 
				Arrays.toString(root.getMax())));
	}
	
	public QStats getStats() {
		QStats s = new QStats();
		if (root != null) {
			root.checkNode(s, null, 0);
		}
		return s;
	}
	
	/**
	 * Statistics container class.
	 */
	public static class QStats {
		int nNodes;
		int maxDepth;
		public int getNodeCount() {
			return nNodes;
		}
		public int getMaxDepth() {
			return maxDepth;
		}
	}
}
