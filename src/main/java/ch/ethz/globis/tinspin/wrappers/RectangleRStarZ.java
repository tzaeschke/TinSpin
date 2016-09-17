/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;

import org.zoodb.index.rtree.DistEntry;
import org.zoodb.index.rtree.RTree;
import org.zoodb.index.rtree.RTreeIterator;
import org.zoodb.index.rtree.RTreeIteratorKnn;

import ch.ethz.globis.tinspin.TestStats;

public class RectangleRStarZ extends Candidate {
	
	private final RTree<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private static final Object O = new Object();
	private RTreeIterator<Object> query = null;
	private RTreeIteratorKnn<Object> queryKnn = null;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public RectangleRStarZ(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		this.phc = RTree.createRStar(dims);
	}
	
	@Override
	public void load(double[] data, int dims) {
		int pos = 0;
		for (int n = 0; n < N; n++) {
			double[] lo = new double[dims];
			double[] hi = new double[dims];
			System.arraycopy(data, pos, lo, 0, dims);
			pos += dims;
			System.arraycopy(data, pos, hi, 0, dims);
			pos += dims;
			phc.insert(lo, hi, O);
		}
		this.data = data;
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		return q;
	}

	@Override
	public int pointQuery(Object qA) {
		int n = 0;
		double[][] dA = (double[][]) qA; 
		for (int i = 0; i < dA.length; i+=2) {
			if (phc.queryEntry(dA[i], dA[i+1]) != null) {
				n++;
			}
		}
		return n;
	}

	@Override
	public boolean supportsPointQuery() {
		return false;
	}
	
	@Override
	public int unload() {
		int n = 0;
		double[] lo = new double[dims];
		double[] hi = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			System.arraycopy(data, i*dims*2, lo, 0, dims);
			System.arraycopy(data, i*dims*2+dims, hi, 0, dims);
			n += phc.remove(lo, hi) != null ? 1 : 0;
			int i2 = N-i-1;
			System.arraycopy(data, i2*dims*2, lo, 0, dims);
			System.arraycopy(data, i2*dims*2+dims, hi, 0, dims);
			n += phc.remove(lo, hi) != null? 1 : 0;
		}
		return n;
	}
	
	
	@Override
	public int query(double[] min, double[] max) {
		if (query == null) {
			query = phc.queryOverlap(min, max);
		} else {
			query.reset(min, max);
		}
		int n = 0;
		while (query.hasNext()) {
			query.next();
			n++;
		}
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		if (queryKnn == null) {
			queryKnn = phc.queryKNN(center, k, null);
		} else {
			queryKnn.reset(center, k, null);
		}
		double ret = 0;
		while (queryKnn.hasNext()) {
			DistEntry<Object> e = queryKnn.next();
			ret += e.dist();
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

	public RTree<Object> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		S.statNnodes = phc.getNodeCount(); 
		S.statNpostlen = phc.getDepth();
	}
	
	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] lo1 = updateTable[i++];
			double[] up1 = updateTable[i++];
			double[] lo2 = Arrays.copyOf(updateTable[i++], dims);
			double[] up2 = Arrays.copyOf(updateTable[i++], dims);
			if (phc.update(lo1, up1, lo2, up2) != null) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public boolean supportsUpdate() {
		return true;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
