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
package org.zoodb.index.quadtree2;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ch.ethz.globis.phtree.PhDistance;
import ch.ethz.globis.phtree.PhFilter;
import ch.ethz.globis.phtree.PhTreeF;
import ch.ethz.globis.phtree.util.PhMapper;

public class PhTreeFQT<T> extends PhTreeF<T> {

	private QuadTreeKD<T> t;
	private final int dims;
	
	private PhTreeFQT(int dims) {
		super(null);
		this.dims = dims;
		this.t = QuadTreeKD.create(dims);
	}
	
	public static <T> PhTreeFQT<T> create(int dim) {
		return new PhTreeFQT<>(dim);
	}
	
	@Override
	public int size() {
		return t.size();
	}

	@Override
	public T put(double[] key, T value) {
		t.put(key, value);
		//Always succeeds, duplicates are allowed
		return null;
	}

	@Override
	public boolean contains(double... key) {
		return t.containsExact(key);
	}

	@Override
	public T get(double... key) {
		return t.getExact(key);
	}

	@Override
	public T remove(double... key) {
		return t.removeExact(key);
	}

	@Override
	public String toStringTree() {
		return t.toStringTree();
	}

	@Override
	public PhExtentF<T> queryExtent() {
		return new PHRSTExtent<>(t, dims);
	}

	private static class PHRSTExtent<T> extends PhExtentF<T> {
		private Iterator<QEntry<T>> iter;
		private QuadTreeKD<T> t;
		private PhEntryF<T> buf;
		private double[] min;
		private double[] max;
		
		public PHRSTExtent(QuadTreeKD<T> t, int dims) {
			super(null, 0, null);
			this.t = t;
			this.buf = new PhEntryF<>(new double[dims], null);
			min = new double[dims];
			max = new double[dims];
			Arrays.fill(min, Double.NEGATIVE_INFINITY);
			Arrays.fill(max, Double.POSITIVE_INFINITY);
			reset();
		}
		
		@Override
		public PhEntryF<T> nextEntryReuse() {
			QEntry<T> p = iter.next();
			System.arraycopy(p.getPoint(), 0, buf.getKey(), 0, p.getPoint().length);
			buf.setValue((T) p.getValue());
			return buf;
		}

		@Override
		public double[] nextKey() {
			QEntry<T> p = iter.next();
			return p.getPoint();
		}

		@Override
		public T nextValue() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhEntryF<T> nextEntry() {
			QEntry<T> p = iter.next();
			return new PhEntryF<>(p.getPoint(), (T) p.getValue());
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
		public PhExtentF<T> reset() {
			iter = t.query(min, max);
			return this;
		}
		
	}

	
	@Override
	public PhQueryF<T> query(double[] min, double[] max) {
		if (dims > 10) {
			throw new UnsupportedOperationException();
		}
		return new PHRSTQuery<>(t, min, max);
	}

	private static class PHRSTQuery<T> extends PhQueryF<T> {
		private Iterator<QEntry<T>> iter;
		private QuadTreeKD<T> t;
		private PhEntryF<T> buf;
		
		public PHRSTQuery(QuadTreeKD<T> t, double[] min, double[] max) {
			super(null, 0, null);
			this.t = t;
			this.buf = new PhEntryF<>(new double[min.length], null);
			reset(min, max);
		}
		
		@Override
		public PhEntryF<T> nextEntryReuse() {
			QEntry<T> p = iter.next();
			System.arraycopy(p.getPoint(), 0, buf.getKey(), 0, p.getPoint().length);
			buf.setValue((T) p.getValue());
			return buf;
		}

		@Override
		public double[] nextKey() {
			QEntry<T> p = iter.next();
			return p.getPoint();
		}

		@Override
		public T nextValue() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhEntryF<T> nextEntry() {
			QEntry<T> p = iter.next();
			return new PhEntryF<>(p.getPoint(), (T) p.getValue());
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
		public void reset(double[] min, double[] max) {
			iter = t.query(min, max);
		}
	}

	
	
	@Override
	public int getDim() {
		return dims;
	}

	@Override
	public PhKnnQueryF<T> nearestNeighbour(int nMin, double... key) {
		return new PHRSTKnnQuery<>(t, nMin, null, key);
	}

//	@Override
//	public PhKnnQueryF<T> nearestNeighbour(int nMin, PhDistance dist, PhFilter dims,
//			double... key) {
//		return new PHRSTKnnQuery<>(this, t, nMin, dist, key);
//	}

	private static class PHRSTKnnQuery<T> extends PhKnnQueryF<T> {
		
		private Iterator<QEntryDist<T>> iter;
		private QuadTreeKD<T> t;
		private PhEntryDistF<T> buf;
		
		public PHRSTKnnQuery(QuadTreeKD<T> t, int nMin, PhDistance dist, double... center) {
			super(null, 0, null);
			this.t = t;
			this.buf = new PhEntryDistF<>(new double[center.length], null, Double.NaN);
			reset(nMin, dist, center);
		}
		
		@Override
		public PhEntryDistF<T> nextEntryReuse() {
			QEntryDist<T> p = iter.next();
			System.arraycopy(p.getPoint(), 0, buf.getKey(), 0, p.getPoint().length);
			buf.setValue((T) p.getValue());
			return buf;
		}

		@Override
		public double[] nextKey() {
			QEntry<T> p = iter.next();
			return p.getPoint();
		}

		@Override
		public T nextValue() {
			return (T) iter.next().getValue();
		}

		@Override
		public PhEntryDistF<T> nextEntry() {
			QEntryDist<T> p = iter.next();
			return new PhEntryDistF<>(p.getPoint(), (T) p.getValue(), p.getDistance());
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
		public PhKnnQueryF<T> reset(int nMin, PhDistance dist, double... center) {
			iter = t.knnSearch(center, nMin).iterator();
			return this;
		}
		
	}

	
	@Override
	public PhRangeQueryF<T> rangeQuery(double dist, double... center) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public PhRangeQueryF<T> rangeQuery(double dist, PhDistance optionalDist, double... center) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public T update(double[] oldKey, double[] newKey) {
		return t.update(oldKey, newKey);
	}

	@Override
	public List<PhEntryF<T>> queryAll(double[] min, double[] max) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public <R> List<R> queryAll(double[] min, double[] max, int maxResults, PhFilter filter, PhMapper<T, R> mapper) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		t.clear();
	}
	
	
}
