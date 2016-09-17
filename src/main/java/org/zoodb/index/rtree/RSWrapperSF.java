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
import java.util.List;

import ch.ethz.globis.phtree.PhDistance;
import ch.ethz.globis.phtree.PhDistanceSF;
import ch.ethz.globis.phtree.PhDistanceSFCenterDist;
import ch.ethz.globis.phtree.PhFilter;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTreeSolidF;
import ch.ethz.globis.phtree.pre.PreProcessorRangeF;
import ch.ethz.globis.phtree.util.PhMapper;

public class RSWrapperSF<T> extends PhTreeSolidF<T> {

	
	private final int dims;
	private final RTree<T> pht;
	
	/**
	 * Create a new tree with the specified number of dimensions.
	 * 
	 * @param dim number of dimensions
	 */
	private RSWrapperSF(int dims) {
		super(dims);
		this.pht = RTree.createRStar(dims);
		this.dims = dims;
	}
	
	/**
	 * Create a new tree with the specified number of dimensions.
	 * 
	 * @param dim number of dimensions
	 * @return new tree
	 * @param <T> value type of the tree
	 */
    public static <T> PhTreeSolidF<T> create(int dim) {
    	return new RSWrapperSF<>(dim);
    }
	
	/**
	 * Inserts a new ranged object into the tree.
	 * @param lower lower left corner
	 * @param upper upper right corner
	 * @param value the value
	 * @return the previous value or {@code null} if no entry existed
	 * 
	 * @see PhTree#put(long[], Object)
	 */
	@Override
	public T put(double[] lower, double[] upper, T value) {
		pht.insert(lower, upper, value);
		return null;
	}
	
	/**
	 * Removes a ranged object from the tree.
	 * @param lower lower left corner
	 * @param upper upper right corner
	 * @return the value or {@code null} if no entry existed
	 * 
	 * @see PhTree#remove(long...)
	 */
	@Override
	public T remove(double[] lower, double[] upper) {
		return pht.remove(lower, upper);
	}
	
	/**
	 * Check whether an entry with the specified coordinates exists in the tree.
	 * @param lower lower left corner
	 * @param upper upper right corner
	 * @return true if the entry was found 
	 * 
	 * @see PhTree#contains(long...)
	 */
	@Override
	public boolean contains(double[] lower, double[] upper) {
		return pht.queryEntry(lower, upper) != null;
	}
	
	/**
	 * @param e the entry
	 * @return any previous value for the key
	 * @see #put(double[], double[], Object)
	 */
	@Override
	public T put(PhEntrySF<T> e) {
		return put(e.lower(), e.upper(), e.value());
	}
	
	/**
	 * @param e the entry
	 * @return the value for the key
	 * @see #remove(double[], double[])
	 */
	@Override
	public T remove(PhEntrySF<T> e) {
		return remove(e.lower(), e.upper());
	}
	
	/**
	 * @param e the entry
	 * @return whether the key exists
	 * @see #contains(double[], double[])
	 */
	@Override
	public boolean contains(PhEntrySF<T> e) {
		return contains(e.lower(), e.upper());
	}
	
	/**
	 * @param e an entry that describes the query rectangle
	 * @return a query iterator
	 * @see #queryInclude(double[], double[])
	 */
	@Override
	public PhQuerySFR<T> queryInclude(PhEntrySF<T> e) {
		return queryInclude(e.lower(), e.upper());
	}
	
	/**
	 * @param e an entry that describes the query rectangle
	 * @return a query iterator
	 * @see #queryIntersect(double[], double[])
	 */
	@Override
	public PhQuerySFR<T> queryIntersect(PhEntrySF<T> e) {
		return queryIntersect(e.lower(), e.upper());
	}
	
	/**
	 * Query for all bodies that are fully included in the query rectangle.
	 * @param lower 'lower left' corner of query rectangle
	 * @param upper 'upper right' corner of query rectangle
	 * @return Iterator over all matching elements.
	 */
	@Override
	public PhQuerySFR<T> queryInclude(double[] lower, double[] upper) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Query for all bodies that are included in or partially intersect with the query rectangle.
	 * @param lower 'lower left' corner of query rectangle
	 * @param upper 'upper right' corner of query rectangle
	 * @return Iterator over all matching elements.
	 */
	@Override
	public PhQuerySFR<T> queryIntersect(double[] lower, double[] upper) {
		return new PhQuerySFR<>(pht.queryOverlap(lower, upper));
	}
	
	/**
	 * Locate nearest neighbours for a given point in space.
	 * @param nMin number of entries to be returned. More entries may or may not be returned if 
	 * several points have the same distance.
	 * @param distanceFunction A distance function for rectangle data. This parameter is optional,
	 * passing a {@code null} will use the default distance function.
	 * @param center the center point
	 * @return The query iterator.
	 */
	@Override
	public PhKnnQuerySFR<T> nearestNeighbour(int nMin, PhDistanceSF distanceFunction,
			double ... center) {
		if (distanceFunction instanceof PhDistanceSFCenterDist) {
			return new PhKnnQuerySFR<>(pht.queryKNN(center, nMin, DistanceFunction.CENTER));
		}
		return new PhKnnQuerySFR<>(pht.queryKNN(center, nMin, null));
	}

	/**
	 * Resetable query result iterator.
	 * @param <T>
	 */
	public static class PhIteratorSFR<T> extends PhIteratorSF<T> {
		protected final RTreeIterator<T> iter;
		private PhIteratorSFR(RTreeIterator<T> iter) {
			super(null, 0, null);
			this.iter = iter;
		}
		@Override
		public boolean hasNext() {
			return iter.hasNext();
		}
		@Override
		public T next() {
			return nextValue();
		}
		@Override
		public T nextValue() {
			return iter.next().value();
		}
		@Override
		public PhEntrySF<T> nextEntry() {
			Entry<T> e = iter.next();
			return new PhEntrySF<>(e.min(), e.max(), e.value());
		}
		@Override
		public PhEntrySF<T> nextEntryReuse() {
			return nextEntry();
		}
		@Override
		public void remove() {
			iter.remove();
		}
	}
	
	public static class PhQuerySFR<T> extends PhQuerySF<T> {
		private final RTreeIterator<T> q;
		
		private PhQuerySFR(RTreeIterator<T> iter) {
			super(null, 0, null, false);
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
		public PhEntrySF<T> nextEntry() {
			Entry<T> e = q.next();
			return new PhEntrySF<>(e.min, e.max, e.val);
		}
		
		@Override
		public PhEntrySF<T> nextEntryReuse() {
			return nextEntry();
		}

		@Override
		public T nextValue() {
			return q.next().value();
		}

		@Override
		public PhQuerySF<T> reset(double[] lower, double[] upper) {
			q.reset(lower, upper);
			return this;
		}
	}
	
	public static class PhKnnQuerySFR<T> extends PhKnnQuerySF<T> {
		private final RTreeIteratorKnn<T> q;
		
		private PhKnnQuerySFR(RTreeIteratorKnn<T> iter) {
			super(null, 0, null);
			this.q = iter;
		}

		/**
		 * Resets the current kNN query with new parameters.
		 * @param nMin
		 * @param newDist Distance function. Supplying 'null' uses the default distance function
		 * for the current preprocessor.
		 * @param center
		 * @return this query instance
		 */
		@Override
		public PhKnnQuerySF<T> reset(int nMin, PhDistance newDist, double[] center) {
			q.reset(center, nMin, null);
			return this;
		}
		
		@Override
		public boolean hasNext() {
			return q.hasNext();
		}
		@Override
		public T next() {
			return nextValue();
		}
		@Override
		public T nextValue() {
			return q.next().value();
		}
		@Override
		public PhEntryDistSF<T> nextEntry() {
            DistEntry<T> e = q.next();
			return new PhEntryDistSF<>(e.min(), e.max, e.value(), e.dist());
		}
		@Override
		public PhEntryDistSF<T> nextEntryReuse() {
			return nextEntry();
		}
		@Override
		public void remove() {
			q.remove();
		}
	}
	
	@Override
	public PhIteratorSFR<T> iterator() {
		return new PhIteratorSFR<>(pht.iterator());
	}

	/**
	 * @param lo1 old min value
	 * @param up1 old max value
	 * @param lo2 new min value
	 * @param up2 new max value
	 * @return true, if the value could be replaced.
	 * @see PhTree#update(long[], long[])
	 */
	@Override
	public T update(double[] lo1, double[] up1, double[] lo2, double[] up2) {
		return pht.update(lo1, up1, lo2, up2);
	}

	/**
	 * Same as {@link #queryIntersect(double[], double[])}, except that it returns a list
	 * instead of an iterator. This may be faster for small result sets. 
	 * @param lower min value
	 * @param upper max value
	 * @return List of query results
	 */
	@Override
	public List<PhEntrySF<T>> queryIntersectAll(double[] lower, double[] upper) {
		return queryIntersectAll(lower, upper, Integer.MAX_VALUE, null,
//				e -> {
//					double[] lo = new double[lower.length]; 
//					double[] up = new double[lower.length]; 
//					pre.post(e.getKey(), lo, up);
//					return new PhEntrySF<>(lo, up, e.getValue());
//				}
				null
				);
	}

	/**
	 * Same as {@link #queryIntersectAll(double[], double[], int, PhFilter, PhMapper)}, 
	 * except that it returns a list instead of an iterator. 
	 * This may be faster for small result sets. 
	 * @param lower min value
	 * @param upper max value
	 * @param maxResults max result count
	 * @param filter filter instance
	 * @param mapper mapper instance for mapping double[] to long[]
	 * @return List of query results
	 * @param <R> result type
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <R> List<R> queryIntersectAll(double[] lower, double[] upper, int maxResults, 
			PhFilter filter, PhMapper<T,R> mapper) {
		RTreeIterator<T> it = pht.queryOverlap(lower, upper);
		ArrayList<R> ret = new ArrayList<>();
		while (it.hasNext()) {
			Entry<T> e = it.next();
			PhEntrySF<T> pe = new PhEntrySF<T>(e.min(), e.max(), e.value());
			ret.add((R) pe);
		}
		return ret;
	}

	/**
	 * @return The number of entries in the tree
	 */
	@Override
	public int size() {
		return pht.size();
	}

	/**
	 * @param lower min value
	 * @param upper max value
	 * @return the element that has 'upper' and 'lower' as key. 
	 */
	@Override
	public T get(double[] lower, double[] upper) {
		return pht.queryEntry(lower, upper);
	}

    /**
     * Clear the tree.
     */
	@Override
	public void clear() {
		pht.clear();
	}

	/**
	 * @return The PhTree that backs this tree.
	 */
	@Override
	public PhTree<T> getInternalTree() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * @return the preprocessor of this tree. 
	 */
	@Override
	public PreProcessorRangeF getPreProcessor() {
		return null;
	}
	
	@Override
	public String toString() {
		return pht.toString(); 
	}

	/**
	 * 
	 * @return the dimensionality of the tree.
	 */
	@Override
	public int getDims() {
		return dims;
	}

}
