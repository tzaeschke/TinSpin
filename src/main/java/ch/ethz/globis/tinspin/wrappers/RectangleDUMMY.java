/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.ArrayList;

import ch.ethz.globis.tinspin.TestStats;

public class RectangleDUMMY extends Candidate {
	
	private final double[][] phc;
	private final int dims;
	private final int N;
	private double[] data;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public RectangleDUMMY(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = new double[2*N][dims];
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
			phc[2*n] = lo;
			phc[2*n+1] = hi;
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
			for (int j = 0; j < N; j+=2) { 
				if (eq(phc[j], dA[i]) && eq(phc[j+1], dA[i+1])) {
					n++;
				}
			}
		}
		return n;
	}

	private boolean eq(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	
	private boolean geq(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] < b[i]) {
				return false;
			}
		}
		return true;
	}
	
	private boolean leq(double[] a, double[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] > b[i]) {
				return false;
			}
		}
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
			n += delete(lo, hi) ? 1 : 0;
			int i2 = N-i-1;
			System.arraycopy(data, i2*dims*2, lo, 0, dims);
			System.arraycopy(data, i2*dims*2+dims, hi, 0, dims);
			n += delete(lo, hi) ? 1 : 0;
		}
		return n;
	}
	
	
	private boolean delete(double[] lo, double[] up) {
		for (int i = 0; i < N; i++) { 
			if (phc[i<<1] != null && eq(phc[i<<1], lo) && eq(phc[(i<<1)+1], up)) {
				phc[i<<1] = null;
				phc[(i<<1)+1] = null;
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public int query(double[] min, double[] max) {
		int n = queryIntersect(min, max).size();
//		Iterator<PHREntry> it = queryIntersect(min, max);
//		int n = 0;
//		while (it.hasNext()) {
//			it.next();
//			n++;
//		}
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	private ArrayList<Integer> queryIntersect(double[] min, double[] max) {
		ArrayList<Integer> results = new ArrayList<>(); 
		for (int i = 0; i < N; i++) { 
			if (leq(phc[i<<1], max) && geq(phc[(i<<1)+1], min)) {
				results.add(i/2);
			}
		}
		return results;
	}

	@Override
	public void release() {
		data = null;
	}

	
	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] lo1 = updateTable[i++];
			double[] up1 = updateTable[i++];
			double[] lo2 = updateTable[i++];
			double[] up2 = updateTable[i++];
			if (update(lo1, up1, lo2, up2)) {
				n++;
			}
		}
		return n;
	}
	
	private boolean update(double[] lo1, double[] up1, double[] lo2, double[] up2) {
		for (int i = 0; i < N; i++) { 
			if (eq(phc[i<<1], lo1) && eq(phc[(i<<1)+1], up1)) {
				phc[i<<1] = lo2;
				phc[(i<<1)+1] = up2;
				return true;
			}
		}
		return false;
	}
}
