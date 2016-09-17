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
package org.zoodb.index.rtree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.ethz.globis.phtree.PhDistance;
import ch.ethz.globis.phtree.PhDistanceF;
import ch.ethz.globis.phtree.PhFilter;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTreeF;
import ch.ethz.globis.phtree.pre.PreProcessorPointF;
import ch.ethz.globis.phtree.util.PhMapper;
import ch.ethz.globis.phtree.util.PhMapperK;
import ch.ethz.globis.phtree.util.PhTreeStats;

public class RSWrapperF<T> extends PhTreeF<T> {

	private final RTree<T> pht;
	
	private RSWrapperF(int dims) {
		super(dims, null);
		pht = RTree.createRStar(dims);
	}

	
	/**
	 * Create a new tree with the specified number of dimensions.
	 * 
	 * @param dims number of dimensions
	 * @return PhTreeF
	 * @param <T> value type of the tree
	 */
	public static <T> PhTreeF<T> create(int dims) {
		return new RSWrapperF<>(dims);
	}

	/**
	 * Create a new tree with the specified number of dimensions and
	 * a custom preprocessor.
	 * 
	 * @param dim number of dimensions
	 * @param pre The preprocessor to be used
	 * @return PhTreeF
	 * @param <T> value type of the tree
	 */
	public static <T> PhTreeF<T> create(int dim, PreProcessorPointF pre) {
		return new RSWrapperF<>(dim);
	}

	/**
	 * Create a new PhTreeF as a wrapper around an existing PhTree.
	 * 
	 * @param tree another tree
	 * @return PhTreeF
	 * @param <T> value type of the tree
	 */
	public static <T> PhTreeF<T> wrap(PhTree<T> tree) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return pht.size();
	}

	/**
	 * Insert an entry associated with a k dimensional key.
	 * @param key the key to store the value to store
	 * @param value the value
	 * @return the previously associated value or {@code null} if the key was found
	 */
	@Override
	public T put(double[] key, T value) {
		pht.insert(key, value);
		return null;
	}

	@Override
	public boolean contains(double ... key) {
		return pht.queryEntry(key, key) != null;
	}

	@Override
	public T get(double ... key) {
		return pht.queryEntry(key, key);
	}


	/**
	 * Remove the entry associated with a k dimensional key.
	 * @param key the key to remove
	 * @return the associated value or {@code null} if the key was found
	 */
	@Override
	public T remove(double... key) {
		return pht.remove(key);
	}

	@Override
	public PhExtentFRS<T> queryExtent() {
		return new PhExtentFRS<>(pht.iterator(), pht.getDims());
	}


	/**
	 * Performs a rectangular window query. The parameters are the min and max keys which 
	 * contain the minimum respectively the maximum keys in every dimension.
	 * @param min Minimum values
	 * @param max Maximum values
	 * @return Result iterator.
	 */
	@Override
	public PhQueryFRS<T> query(double[] min, double[] max) {
		return new PhQueryFRS<>(pht.queryOverlap(min, max));
	}

	/**
	 * Find all entries within a given distance from a center point.
	 * @param dist Maximum distance
	 * @param center Center point
	 * @return All entries with at most distance `dist` from `center`.
	 */
	@Override
	public PhRangeQueryFRS<T> rangeQuery(double dist, double...center) {
		return rangeQuery(dist, PhDistanceF.THIS, center);
	}

	/**
	 * Find all entries within a given distance from a center point.
	 * @param dist Maximum distance
	 * @param optionalDist Distance function, optional, can be `null`.
	 * @param center Center point
	 * @return All entries with at most distance `dist` from `center`.
	 */
	@Override
	public PhRangeQueryFRS<T> rangeQuery(double dist, PhDistance optionalDist, double...center) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getDim() {
		return pht.getDims();
	}

	/**
	 * Locate nearest neighbours for a given point in space.
	 * @param nMin number of entries to be returned. More entries may or may not be returned if 
	 * several points have the same distance.
	 * @param key the center point
	 * @return List of neighbours.
	 */
	@Override
	public PhKnnQueryFRS<T> nearestNeighbour(int nMin, double... key) {
		return new PhKnnQueryFRS<>(pht.queryKNN(key, nMin, null));
	}

	/**
	 * Locate nearest neighbours for a given point in space.
	 * @param nMin number of entries to be returned. More entries may or may not be returned if 
	 * several points have the same distance.
	 * @param dist Distance function. Note that the distance function should be compatible
	 * with the preprocessor of the tree.
	 * @param key the center point
	 * @return KNN query iterator.
	 */
	@Override
	public PhKnnQueryFRS<T> nearestNeighbour(int nMin, PhDistance dist, double... key) {
		return new PhKnnQueryFRS<>(pht.queryKNN(key, nMin, null));
	}

	public static class PhExtentFRS<T> extends PhExtentF<T> {
		private final RTreeIterator<T> q;
		private final double[] min;
		private final double[] max;
		protected PhExtentFRS(RTreeIterator<T> iter, int dims) {
			super(null, 0, null);
			this.q = iter;
			min = new double[dims];
			max = new double[dims];
			Arrays.fill(min, Double.NEGATIVE_INFINITY);
			Arrays.fill(max, Double.POSITIVE_INFINITY);
		}		
		
		@Override
		public boolean hasNext() {
			return q.hasNext();
		}

		@Override
		public T next() {
			return q.next().value();
		}
		
		@Override
		public PhEntryF<T> nextEntry() {
			Entry<T> e = q.next();
			return new PhEntryF<>(e.min, e.val);
		}
		
		@Override
		public PhEntryF<T> nextEntryReuse() {
			return nextEntry();
		}

		@Override
		public T nextValue() {
			return q.next().value();
		}

		@Override
		public double[] nextKey() {
			return q.next().min();
		}
		
		@Override
		public PhExtentFRS<T> reset() {
			q.reset(min, max);
			return this;
		}
	}
	
	public static class PhQueryFRS<T> extends PhQueryF<T> {
		private final RTreeIterator<T> q;

		protected PhQueryFRS(RTreeIterator<T> iter) {
			super(null, 0, null);
			q = iter;
		}

		@Override
		public boolean hasNext() {
			return q.hasNext();
		}

		@Override
		public T next() {
			return q.next().value();
		}
		
		@Override
		public PhEntryF<T> nextEntry() {
			Entry<T> e = q.next();
			return new PhEntryF<>(e.min, e.val);
		}
		
		@Override
		public PhEntryF<T> nextEntryReuse() {
			return nextEntry();
		}

		@Override
		public T nextValue() {
			return q.next().value();
		}

		@Override
		public double[] nextKey() {
			return q.next().min();
		}
		
		@Override
		public void reset(double[] lower, double[] upper) {
			q.reset(lower, upper);
		}
	}

	public static class PhKnnQueryFRS<T> extends PhKnnQueryF<T> {
		private final RTreeIteratorKnn<T> q;

		protected PhKnnQueryFRS(RTreeIteratorKnn<T> iter) {
			super(null, 0, null);
			q = iter;
		}
		
		@Override
		public PhEntryDistF<T> nextEntry() {
			DistEntry<T> e = q.next();
			return new PhEntryDistF<>(e.min(), e.value(), e.dist());
		}

		@Override
		public PhEntryDistF<T> nextEntryReuse() {
			return nextEntry();
		}

		@Override
		public boolean hasNext() {
			return q.hasNext();
		}

		@Override
		public T next() {
			return q.next().value();
		}

		@Override
		public T nextValue() {
			return q.next().value();
		}

		@Override
		public double[] nextKey() {
			return q.next().min();
		}
		
		@Override
		public PhKnnQueryF<T> reset(int nMin, PhDistance dist, double... center) {
			q.reset(center, nMin, null);
			return this;
		}
	}

	public static class PhRangeQueryFRS<T> extends PhRangeQueryF<T> {
		private final RTreeIterator<T> q;

		protected PhRangeQueryFRS(RTreeIterator<T> iter) {
			super(null, null, null);
			this.q = iter;
		}

		@Override
		public PhRangeQueryFRS<T> reset(double range, double... center) {
			throw new UnsupportedOperationException();
			//q.reset(range, lCenter);
			//return this;
		}
	}

	/**
	 * Update the key of an entry. Update may fail if the old key does not exist, or if the new
	 * key already exists.
	 * @param oldKey old key
	 * @param newKey new key
	 * @return the value (can be {@code null}) associated with the updated key if the key could be 
	 * updated, otherwise {@code null}.
	 */
	@Override
	public T update(double[] oldKey, double[] newKey) {
		return pht.update(oldKey, oldKey, newKey, newKey);
	}

	/**
	 * Same as {@link #query(double[], double[])}, except that it returns a list
	 * instead of an iterator. This may be faster for small result sets. 
	 * @param min min values
	 * @param max max values
	 * @return List of query results
	 */
	@Override
	public List<PhEntryF<T>> queryAll(double[] min, double[] max) {
		return queryAll(min, max, Integer.MAX_VALUE, null,
				e -> new PhEntryF<T>(PhMapperK.toDouble(e.getKey()), e.getValue()));
	}

	/**
	 * Same as {@link PhTreeF#queryAll(double[], double[])}, except that it also accepts
	 * a limit for the result size, a filter and a mapper. 
	 * @param min min key
	 * @param max max key
	 * @param maxResults maximum result count 
	 * @param filter filter object (optional)
	 * @param mapper mapper object (optional)
	 * @return List of query results
	 */
	@Override
	public <R> List<R> queryAll(double[] min, double[] max, int maxResults, 
			PhFilter filter, PhMapper<T, R> mapper) {
		RTreeIterator<T> it = pht.queryOverlap(min, max);
		ArrayList<R> ret = new ArrayList<>();
		while (it.hasNext()) {
			Entry<T> e = it.next();
			PhEntryF<T> pe = new PhEntryF<T>(e.min(), e.value());
			ret.add((R) pe);
		}
		return ret;
	}

	/**
	 * Clear the tree.
	 */
	@Override
	public void clear() {
		pht.clear();
	}

	/**
	 * 
	 * @return the internal PhTree that backs this PhTreeF.
	 */
	@Override
	public PhTree<T> getInternalTree() {
		throw new UnsupportedOperationException();
	}

	/**
	 * 
	 * @return the preprocessor of this tree.
	 */
	@Override
	public PreProcessorPointF getPreprocessor() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @return A string tree view of all entries in the tree.
	 * @see PhTree#toStringTree()
	 */
	@Override
	public String toStringTree() {
		return pht.toStringTree();
	}

	@Override
	public String toString() {
		return pht.toString(); 
	}

	@Override
	public PhTreeStats getStats() {
		pht.getStats();
		return null;
	}
}
