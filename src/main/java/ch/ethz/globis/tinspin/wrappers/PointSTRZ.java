/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;

import org.tinspin.index.Index;
import org.tinspin.index.rtree.*;
import org.tinspin.index.rtree.RTree.RTreeStats;

import ch.ethz.globis.tinspin.TestStats;

/**
 * R-Tree with sort tile recursive bulk loading.
 *
 */
public class PointSTRZ extends Candidate {
	
	private final RTree<Integer> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private RTreeIterator<Integer> it;
	private RTreeQueryKnn2<Integer> itKnn;

	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointSTRZ(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = RTree.createRStar(dims);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void load(double[] data, int dims) {
		int pos = 0;
		@SuppressWarnings("rawtype")
		Entry<Integer>[] list = (Entry<Integer>[]) new Entry[N];
		for (int n = 0; n < N; n++) {
			double[] lo = new double[dims];
			double[] hi = new double[dims];
			System.arraycopy(data, pos, lo, 0, dims);
			System.arraycopy(data, pos, hi, 0, dims);
			pos += dims;
			Entry<Integer> e = new Entry<>(lo, hi, n);
			list[n] = e;
		}
		phc.load(list);
		this.data = data;
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		return q;
	}

	@Override
	public int pointQuery(Object qA, int[] ids) {
		int n = 0;
		for (double[] q: (double[][])qA) {
			if (phc.queryExact(q, q) != null) {
				n++;
			}
		}
		return n;
	}

	@Override
	public int unload() {
		int n = 0;
		double[] l = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
			n += phc.remove(getEntry(l, N-i-1)) != null ? 1 : 0;
		}
		if ((N%2) != 0) {
			int i = (N>>1);
			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
		}
		return n;
	}

	private double[] getEntry(double[] val, int pos) {
		for (int d = 0; d < dims; d++) {
			val[d] = data[pos*dims+d];
		}
		return val;
	}
	
	@Override
	public int query(double[] min, double[] max) {
		if (it == null) {
			it = phc.queryIntersect(min, max);
		} else {
			it.reset(min, max);
		}
		int n = 0;
		while (it.hasNext()) {
			it.next();
			n++;
		}
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		if (k == 1) {
			return phc.query1nn(center).dist();
		}
		if (itKnn == null) {
			itKnn = phc.queryKnn(center, k);
		} else {
			itKnn.reset(center, k);
		}
		double ret = 0;
		while (itKnn.hasNext()) {
			ret += itKnn.next().dist();
		}
		return ret;
	}

	@Override
	public boolean supportsKNN() {
		return true;
	}
	
	@Override
	public void release() {
		data = null;
	}

	
	/**
	 * Used to test the native code during development process
	 */
	@Override
	public Index getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats s) {
		RTreeStats qs = phc.getStats();
		s.statNnodes = qs.getNodeCount();
		s.statNpostlen = qs.getMaxDepth();
		s.statNDistCalc = qs.getNDistCalc();
		s.statNDistCalc1NN = qs.getNDistCalc1NN();
	}
	
	@Override
	public int update(double[][] updateTable, int[] ids) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = Arrays.copyOf(updateTable[i++], dims);
			if (phc.update(p1, p1, p2, p2) != null) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public boolean supportsPointQuery() {
		return dims <= 16;
	}
	
	@Override
	public boolean supportsUpdate() {
		return dims <= 16;
	}

	@Override
	public boolean supportsUnload() {
		return dims <= 16;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
