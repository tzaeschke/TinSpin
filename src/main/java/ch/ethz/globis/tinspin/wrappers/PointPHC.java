/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.phtree.PhDistance;
import ch.ethz.globis.phtree.PhDistanceF;
import ch.ethz.globis.phtree.PhDistanceF_L1;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTree.PhKnnQuery;
import ch.ethz.globis.phtree.PhTree.PhQuery;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.phtree.v11.PhTree11;
import ch.ethz.globis.phtree.v13.PhTree13;
import ch.ethz.globis.tinspin.TestStats;

/**
 * Test adapter for PH-Tree.
 * 
 */
public class PointPHC extends CandidatePHC {
	
	private static final Object O = new Object();
	
	private final PhTree<Object> phc;
	private final int dims;
    private int N;
	private double[] data;
	private PhQuery<Object> query;
    private final PhDistance distanceKnn;
	private long[] qMin;
	private long[] qMax;
	private PhKnnQuery<Object> knnQuery;
	private long[] knnCenter;
	private TestStats S;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts TestStats
	 */
	public PointPHC(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		this.S = ts;
        distanceKnn = PhDistanceF.THIS;
        //distanceKnn = PhDistanceF_L1.THIS;
//		phc = new PhTree11<Object>(ts.cfgNDims);
		phc = PhTree.create(ts.cfgNDims);
//		Node.AHC_LHC_BIAS = 1*1000*1000;
//		Node.NT_THRESHOLD = 2*1000*1000;
//		PhTree11.HCI_ENABLED = true;
//		TestRunner.USE_NEW_QUERIES = false;
		qMin = new long[dims];
		qMax = new long[dims];
		knnCenter = new long[dims];
		query = phc.query(qMin, qMax);
		knnQuery = phc.nearestNeighbour(1, knnCenter);
	}
	
	@Override
	public void load(double[] data, int dims) {
		int nDuplicates = 0;
		long[] buf = new long[dims];
		for (int i = 0; i < N; i++) {
			for (int d = 0; d < dims; d++) {
				buf[d] = f2l(data[i*dims+d]); 
			}
			if (phc.put(buf, O) != null) {
				//throw new IllegalArgumentException("i=" + i + " " + Arrays.toString(buf));
				nDuplicates++;
			}
		}
        if (nDuplicates > 0) {
            System.err.println("**************************   DUPLICATES FOUND: " + nDuplicates);
			N -= nDuplicates;
			S.cfgNEntries -= nDuplicates;
		}
        this.data = data;
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		long[][] data = new long[q.length][dims];
		
		for (int i=0; i<q.length; i++) {
			f2l(q[i], data[i]);
		}
		return data;
	}

	@Override
	public int pointQuery(Object qA) {
		int n = 0;
		for (long[] q: (long[][])qA) {
			if (phc.contains(q)) {
				n++;
			}
			//log("q=" + Arrays.toString(q));
		}
		return n;
	}

	@Override
	public int unload() {
		int n = 0;
		long[] l = new long[dims];
		//Simulate semi-random removal
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

	private long[] getEntry(long[] val, int pos) {
		for (int d = 0; d < dims; d++) {
			val[d] = f2l(data[pos*dims+d]);
		}
		return val;
	}
	
	@Override
	public int query(double[] min, double[] max) {
		f2l(min, qMin);
		f2l(max, qMax);
		query.reset(qMin, qMax);
		int n = 0;
		while (query.hasNext()) {
			query.nextValue();
			n++;
		}
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		f2l(center, knnCenter);
        knnQuery.reset(k, distanceKnn, knnCenter);
		double ret = 0;
		double[] v2 = new double[dims];
		int n = 0;
		while (knnQuery.hasNext() && n++ < k) {
			long[] v = knnQuery.nextEntryReuse().getKey();
			l2f(v, v2);
			ret += dist(center, v2);
		}
		if (n < k) {
			throw new IllegalStateException("n/k=" + n + "/" + k);
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
	public PhTree<Object> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		PhTreeStats q = phc.getStats();
		setStats(S, q);
		System.out.println(q);
		System.out.println(q.toStringHist());
	}
	
	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		long[] oldKey = new long[dims];
		long[] newKey = new long[dims];
		for (int i = 0; i < updateTable.length; ) {
			double[] pOld = updateTable[i++];
			double[] pNew = updateTable[i++];
			for (int d = 0; d < dims; d++) {
				oldKey[d] = f2l(pOld[d]); 
				newKey[d] = f2l(pNew[d]); 
			}
			if (phc.update(oldKey, newKey) != null) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public String toString() {
        return phc.toString() + "dist=" + distanceKnn;
	}
}
