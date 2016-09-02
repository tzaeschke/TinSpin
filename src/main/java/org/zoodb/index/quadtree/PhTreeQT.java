/*
 * Copyright 2016 Tilmann Zäschke
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
import java.util.List;

import ch.ethz.globis.phtree.PhDistance;
import ch.ethz.globis.phtree.PhEntry;
import ch.ethz.globis.phtree.PhFilter;
import ch.ethz.globis.phtree.PhRangeQuery;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.phtree.util.PhMapper;
import ch.ethz.globis.phtree.util.PhTreeStats;

/**
 * 
 * @param <T>
 */
public class PhTreeQT<T> implements PhTree<T> {

	private QuadTreeKD<T> t;
	private final int dims;
	private int conversionType = CT_NONE;
	private static final int CT_NONE = -1;
	private static final int CT_CAST = 1;
	private static final int CT_IEEE = 2;
	
	private PhTreeQT(int dims) {
		this.dims = dims;
		this.t = QuadTreeKD.create(dims);
	}
	
	public static <T> PhTreeQT<T> create(int dim) {
		return new PhTreeQT<>(dim);
	}
	
	private void toDouble(long[] la, double[] da) {
		if (conversionType == CT_NONE) {
			for (long l : la) {
				if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE) {
					//this is a converter
					conversionType = CT_IEEE;
					break;
				} 
				conversionType = CT_CAST;
				break;
			}
		}
		if (conversionType == CT_CAST) {
			for (int i = 0; i < la.length; i++) {
				da[i] = la[i];
			}
		} else {
			for (int i = 0; i < la.length; i++) {
				da[i] = BitTools.toDouble(la[i]);
			}
		}
	}
	
	private void toLong(double[] da, long[] la) {
		if (conversionType == CT_NONE) {
			throw new IllegalStateException();
		}
		if (conversionType == CT_CAST) {
			for (int i = 0; i < la.length; i++) {
				la[i] = (long) da[i];
			}
		} else {
			for (int i = 0; i < la.length; i++) {
				la[i] = BitTools.toSortableLong(da[i]);
			}
		}
	}
	
	
	@Override
	public int size() {
		return t.size();
	}

	@Override
	public PhTreeStats getStats() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T put(long[] key, T value) {
		double[] x = new double[dims];
		toDouble(key, x);
		t.put(x, value);
		//Always succeeds, duplicates are allowed
		return null;
	}

	@Override
	public boolean contains(long... key) {
		double[] x = new double[dims];
		toDouble(key, x);
		return t.containsExact(x);
	}

	@Override
	public T get(long... key) {
		double[] x = new double[dims];
		toDouble(key, x);
		return t.getExact(x);
	}

	@Override
	public T remove(long... key) {
		double[] x = new double[dims];
		toDouble(key, x);
		return t.removeExact(x);
	}

	@Override
	public String toStringPlain() {
		return t.toStringTree();
	}

	@Override
	public String toStringTree() {
		return t.toStringTree();
	}

	@Override
	public PhExtent<T> queryExtent() {
		return new PHRSTExtent<>(this, t, dims, conversionType);
	}

	private static class PHRSTExtent<T> implements PhExtent<T> {
		private Iterator<QEntry<T>> iter;
		private QuadTreeKD<T> t;
		private PhEntry<T> buf;
		private double[] min;
		private double[] max;
		private PhTreeQT<T> pt;
		
		public PHRSTExtent(PhTreeQT<T> pt, QuadTreeKD<T> t, int dims, int conversionType) {
			this.pt = pt;
			this.t = t;
			this.buf = new PhEntry<>(new long[dims], null);
			min = new double[dims];
			max = new double[dims];
//			if (conversionType == CT_IEEE) {
				Arrays.fill(min, Double.NEGATIVE_INFINITY);
				Arrays.fill(max, Double.POSITIVE_INFINITY);
//			} else {
//				Arrays.fill(min, Integer.MIN_VALUE);
//				Arrays.fill(max, Integer.MAX_VALUE);
//			}
			reset();
		}
		
		@Override
		public PhEntry<T> nextEntryReuse() {
			QEntry<T> p = iter.next();
			pt.toLong(p.getPoint(), buf.getKey());
			buf.setValue((T) p.getValue());
			return buf;
		}

		@Override
		public long[] nextKey() {
			QEntry<T> p = iter.next();
			long[] coord = new long[p.getPoint().length];
			pt.toLong(p.getPoint(), coord);
			return coord;
		}

		@Override
		public T nextValue() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhEntry<T> nextEntry() {
			QEntry<T> p = iter.next();
			long[] coord = new long[p.getPoint().length];
			pt.toLong(p.getPoint(), coord);
			return new PhEntry<>(coord, (T) p.getValue());
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public T next() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhExtent<T> reset() {
			iter = t.query(min, max);
			return this;
		}
		
	}

	
	@Override
	public PhQuery<T> query(long[] min, long[] max) {
		if (dims > 10) {
			throw new UnsupportedOperationException();
		}
		return new PHRSTQuery<>(this, t, min, max);
	}

	private static class PHRSTQuery<T> implements PhQuery<T> {
		
		private Iterator<QEntry<T>> iter;
		private QuadTreeKD<T> t;
		private PhEntry<T> buf;
		private final PhTreeQT<T> pt;
		
		public PHRSTQuery(PhTreeQT<T> pt, QuadTreeKD<T> t, long[] min, long[] max) {
			this.pt = pt;
			this.t = t;
			this.buf = new PhEntry<>(new long[min.length], null);
			reset(min, max);
		}
		
		@Override
		public PhEntry<T> nextEntryReuse() {
			QEntry<T> p = iter.next();
			pt.toLong(p.getPoint(), buf.getKey());
			buf.setValue((T) p.getValue());
			return buf;
		}

		@Override
		public long[] nextKey() {
			QEntry<T> p = iter.next();
			long[] coord = new long[p.getPoint().length];
			pt.toLong(p.getPoint(), coord);
			return coord;
		}

		@Override
		public T nextValue() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhEntry<T> nextEntry() {
			QEntry<T> p = iter.next();
			long[] coord = new long[p.getPoint().length];
			pt.toLong(p.getPoint(), coord);
			return new PhEntry<>(coord, (T) p.getValue());
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public T next() {
			return (T) iter.next().getValue();
		}

		@Override
		public void reset(long[] min, long[] max) {
			double[] lo = new double[min.length];
			double[] up = new double[max.length];
			pt.toDouble(min, lo);
			pt.toDouble(max, up);
			iter = t.query(lo, up);
		}
		
	}

	
	
	@Override
	public int getDim() {
		return dims;
	}

	@Override
	public int getBitDepth() {
		return 64;
	}

	@Override
	public PhKnnQuery<T> nearestNeighbour(int nMin, long... key) {
		return new PHRSTKnnQuery<>(this, t, nMin, null, key);
	}

	@Override
	public PhKnnQuery<T> nearestNeighbour(int nMin, PhDistance dist, PhFilter dims,
			long... key) {
		return new PHRSTKnnQuery<>(this, t, nMin, dist, key);
	}

	private static class PHRSTKnnQuery<T> implements PhKnnQuery<T> {
		
		private Iterator<QEntryDist<T>> iter;
		private QuadTreeKD<T> t;
		private final PhTreeQT<T> pt;
		private PhEntry<T> buf;
		
		public PHRSTKnnQuery(
				PhTreeQT<T> pt, QuadTreeKD<T> t, int nMin, PhDistance dist, long... center) {
			this.t = t;
			this.pt = pt;
			this.buf = new PhEntry<>(new long[center.length], null);
			reset(nMin, dist, center);
		}
		
		@Override
		public PhEntry<T> nextEntryReuse() {
			QEntry<T> p = iter.next();
			pt.toLong(p.getPoint(), buf.getKey());
			buf.setValue((T) p.getValue());
			return buf;
		}

		@Override
		public long[] nextKey() {
			QEntry<T> p = iter.next();
			long[] coord = new long[p.getPoint().length];
			pt.toLong(p.getPoint(), coord);
			return coord;
		}

		@Override
		public T nextValue() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhEntry<T> nextEntry() {
			QEntry<T> p = iter.next();
			long[] coord = new long[p.getPoint().length];
			pt.toLong(p.getPoint(), coord);
			return new PhEntry<>(coord, (T) p.getValue());
		}

		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}

		@Override
		public T next() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhKnnQuery<T> reset(int nMin, PhDistance dist, long... center) {
			double[] cd = new double[center.length];
			pt.toDouble(center, cd);
			iter = t.knnSearch(cd, nMin).iterator();
			return this;
		}
		
	}

	
	@Override
	public PhRangeQuery<T> rangeQuery(double dist, long... center) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public PhRangeQuery<T> rangeQuery(double dist, PhDistance optionalDist, long... center) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public T update(long[] oldKey, long[] newKey) {
		double[] oldD = new double[dims];
		double[] newD = new double[dims];
		toDouble(oldKey, oldD);
		toDouble(newKey, newD);
		return t.update(oldD, newD);
	}

	@Override
	public List<PhEntry<T>> queryAll(long[] min, long[] max) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public <R> List<R> queryAll(long[] min, long[] max, int maxResults, PhFilter filter, 
			PhMapper<T, R> mapper) {
		ArrayList<R> ret = new ArrayList<>();
		double[] minD = new double[dims];
		double[] maxD = new double[dims];
		toDouble(min, minD);
		toDouble(max, maxD);
		Iterator<QEntry<T>> it = t.query(minD, maxD);
		while (it.hasNext() && ret.size() < maxResults) {
			QEntry<T> qe = it.next();
			long[] pl = new long[dims];
			toLong(qe.getPoint(), pl);
			PhEntry<T> pe = new PhEntry<>(pl, qe.getValue());
			ret.add(mapper.map(pe));
		}
		return ret;
	}

	@Override
	public void clear() {
		t.clear();
	}

}
