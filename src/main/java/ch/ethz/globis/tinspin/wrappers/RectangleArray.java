/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import ch.ethz.globis.tinspin.TestStats;

public class RectangleArray extends Candidate {
	
	private final double[][] phc;
	private final int dims;
	private final int N;
	private double[] data;
	
	/**
	 * Setup of an simple array data structure (no indexing).
	 * 
	 * @param ts test stats
	 */
	public RectangleArray(TestStats ts) {
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
			for (int j = 0; j < phc.length; j+=2) { 
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
	public int query(double[] min, double[] max) {
		int n = 0; 
		for (int i = 0; i < N; i++) { 
			if (leq(phc[i*2], max) && geq(phc[(i*2)+1], min)) {
				n++;
			}
		}
		return n;
	}

	@Override
	public double knnQuery(int k, double[] center) {
		ArrayList<KnnEntry> ret = new ArrayList<>(k);
		for (int i = 0; i < phc.length/2; i++) {
			double[] min = phc[i*2];
			double[] max = phc[i*2+1];
			double dist = dist(center, min, max);
			if (ret.size() < k) {
				ret.add(new KnnEntry(min, max, dist));
				ret.sort(COMP);
			} else if (ret.get(k-1).dist > dist) {
				ret.remove(k-1);
				ret.add(new KnnEntry(min, max, dist));
				ret.sort(COMP);
			}
		}
		
		//check
		double totalDist = 0;
		int n = 0;
        for (int i = 0; i < ret.size(); i++) {
        	KnnEntry e = ret.get(i);
        	totalDist += e.dist;
        	n++;
        	if (n==k) {
        		break;
        	}
        }
        if (n < k) {
        	throw new IllegalStateException("n/k=" + n + "/" + k);
        }
		return totalDist;
	}
	
	
	private double dist(double[] k, double[] min, double[] max) {
		return distREdge(k, min, max);
		//return distRCenter(k, min, max);
	} 
	
	private static final Comparator<KnnEntry> COMP = new Comparator<KnnEntry>() {
		@Override
		public int compare(KnnEntry o1, KnnEntry o2) {
			return o1.compareTo(o2);
		}
	};
	
	private static class KnnEntry implements Comparable<KnnEntry> {
		private final double[] min;
		private final double[] max;
		private final double dist;
		KnnEntry(double[] min, double[] max, double dist) {
			this.min = min;
			this.max = max;
			this.dist = dist;
		}
		@Override
		public int compareTo(KnnEntry o) {
			double d = dist-o.dist;
			return d < 0 ? -1 : d > 0 ? 1 : 0;
		}
		
		@Override
		public String toString() {
			return "d=" + dist + ":" + Arrays.toString(min) + "/" + Arrays.toString(max);
		}
		
	}
	
	@Override
	public boolean supportsKNN() {
		return true;
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
			if (eq(phc[i*2], lo1) && eq(phc[(i*2)+1], up1)) {
				System.arraycopy(lo2, 0, phc[i*2], 0, dims);
				System.arraycopy(up2, 0, phc[(i*2)+1], 0, dims);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean supportsUpdate() {
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
			if (phc[i*2] != null && eq(phc[i*2], lo) && eq(phc[(i*2)+1], up)) {
				phc[i*2] = null;
				phc[(i*2)+1] = null;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "NaiveArray";
	}
}
