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

public class PointArray extends Candidate {
	
	private final double[][] phc;
	private final int dims;
	private final int N;
	private double[] data;
	
	/**
	 * Setup of an simple array data structure (no indexing).
	 * 
	 * @param ts test stats
	 */
	public PointArray(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = new double[N][dims];
	}
	
	@Override
	public void load(double[] data, int dims) {
		int pos = 0;
		for (int n = 0; n < N; n++) {
			System.arraycopy(data, pos, phc[n], 0, dims);
			pos += dims;
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
		for (int i = 0; i < dA.length; i++) {
			for (int j = 0; j < N; j++) { 
				if (eq(phc[j], dA[i])) {
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
		int n = queryPoints(min, max).size();
//		List<Integer> list = queryPoints(min, max);
//		int n = 0;
//		while (it.hasNext()) {
//			it.next();
//			n++;
//		}
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	private ArrayList<Integer> queryPoints(double[] min, double[] max) {
		ArrayList<Integer> results = new ArrayList<>(); 
		for (int i = 0; i < N; i++) { 
			if (leq(phc[i], max) && geq(phc[i], min)) {
				results.add(i);
			}
		}
		return results;
	}

	@Override
	public double knnQuery(int k, double[] center) {
		ArrayList<KnnEntry> ret = new ArrayList<>(k);
		for (int i = 0; i < phc.length; i++) {
			double[] p = phc[i];
			double dist = dist(center, p);
			if (ret.size() < k) {
				ret.add(new KnnEntry(p, dist));
				ret.sort(COMP);
			} else if (ret.get(k-1).dist > dist) {
				ret.remove(k-1);
				ret.add(new KnnEntry(p, dist));
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
	
	private static final Comparator<KnnEntry> COMP = new Comparator<KnnEntry>() {
		@Override
		public int compare(KnnEntry o1, KnnEntry o2) {
			return o1.compareTo(o2);
		}
	};
	
	private static class KnnEntry implements Comparable<KnnEntry> {
		private final double[] p;
		private final double dist;
		KnnEntry(double[] p, double dist) {
			this.p = p;
			this.dist = dist;
		}
		@Override
		public int compareTo(KnnEntry o) {
			double d = dist-o.dist;
			return d < 0 ? -1 : d > 0 ? 1 : 0;
		}
		
		@Override
		public String toString() {
			return "d=" + dist + ":" + Arrays.toString(p);
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
	public int update(double[][] updateTable, int[] ids) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = updateTable[i++];
			if (update(p1, p2)) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public boolean supportsUpdate() {
		return true;
	}
	
	private boolean update(double[] p1, double[] p2) {
		for (int i = 0; i < N; i++) { 
			if (eq(phc[i], p1)) {
				System.arraycopy(p2, 0, phc[i], 0, dims);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int unload() {
		int n = 0;
		double[] p = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			System.arraycopy(data, i*dims, p, 0, dims);
			n += delete(p) ? 1 : 0;
			int i2 = N-i-1;
			System.arraycopy(data, i2*dims, p, 0, dims);
			n += delete(p) ? 1 : 0;
		}
		if ((N%2) != 0) {
			int i = (N>>1);
			System.arraycopy(data, i*dims, p, 0, dims);
			n += delete(p) ? 1 : 0;
		}
		return n;
	}
	
	
	private boolean delete(double[] p) {
		for (int i = 0; i < N; i++) { 
			if (phc[i] != null && eq(phc[i], p)) {
				phc[i] = null;
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
