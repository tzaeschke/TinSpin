/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;
import java.util.List;

import org.tinspin.index.qthypercube.QREntryDist;
import org.tinspin.index.qthypercube.QRIterator;
import org.tinspin.index.qthypercube.QuadTreeRKD;
import org.tinspin.index.qthypercube.QuadTreeKD.QStats;

import ch.ethz.globis.tinspin.TestStats;

public class RectangleQuadZ extends Candidate {
	
	private QuadTreeRKD<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private static final Object O = new Object();
	private QRIterator<Object> query = null;
	private final int maxNodeSize = 10;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public RectangleQuadZ(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		//this.phc = QuadTreeRKD.create(dims);
	}
	
	@Override
	public void load(double[] data, int dims) {
		double[] min = new double[dims];
		double[] max = new double[dims];
		Arrays.fill(min, Double.POSITIVE_INFINITY);
		Arrays.fill(max, Double.NEGATIVE_INFINITY);
		for (int i = 0; i < 2*N; i+=dims) {
			for (int d = 0; d < dims; d++) {
				double x = data[i*dims+d];
				if (x > max[d]) {
					max[d] = x;
				}
				if (x < min[d]) {
					min[d] = x;
				}
			}
		}

		this.phc = QuadTreeRKD.create(dims, maxNodeSize, min, max);
		
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
	public int pointQuery(Object qA, int[] ids) {
		int n = 0;
		double[][] dA = (double[][]) qA; 
		for (int i = 0; i < dA.length; i+=2) {
			if (phc.containsExact(dA[i], dA[i+1])) {
				n++;
			}
		}
		return n;
	}

	@Override
	public boolean supportsPointQuery() {
		return true;
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
		if ((N%2) != 0) {
			int i = (N>>1);
			System.arraycopy(data, i*dims*2, lo, 0, dims);
			System.arraycopy(data, i*dims*2+dims, hi, 0, dims);
			n += phc.remove(lo, hi) != null ? 1 : 0;
		}
		return n;
	}
	
	
	@Override
	public int query(double[] min, double[] max) {
		if (query == null) {
			query = phc.queryIntersect(min, max);
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
		List<QREntryDist<Object>> result = phc.knnQuery(center, k);
		double ret = 0;
		for (int i = 0; i < k; i++) {
			QREntryDist<Object> e = result.get(i);
			ret += e.dist();
		}
		return ret;
	}

	@Override
	public boolean supportsKNN() {
		return dims <= 15;
	}
	
	@Override
	public void release() {
		data = null;
	}

	@Override
	public QuadTreeRKD<Object> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		QStats qs = phc.getStats();
		S.statNnodes = qs.getNodeCount(); 
		S.statNpostlen = qs.getMaxDepth();
	}
	


	@Override
	public int update(double[][] updateTable, int[] ids) {
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
