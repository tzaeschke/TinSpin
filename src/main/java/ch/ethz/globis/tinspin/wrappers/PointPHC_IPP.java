/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.phtree.PhDistance;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTreeHelper;
import ch.ethz.globis.phtree.PhTree.PhKnnQuery;
import ch.ethz.globis.phtree.PhTree.PhQuery;
import ch.ethz.globis.phtree.pre.PreProcessorPointF;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.phtree.v11.PhTree11;
import ch.ethz.globis.phtree.v13.PhTree13;
import ch.ethz.globis.phtree.v16.PhTree16;
import ch.ethz.globis.phtree.v16hd.PhTree16HD;
import ch.ethz.globis.tinspin.TestStats;

public class PointPHC_IPP extends CandidatePHC {
	
	private static final Object O = new Object();
	
	private final PhTree<Object> phc;
	private final int dims;
	private final int N;
	private PreProcessorPointF pp;
	private PhDistance dist;
	private double[] data;
	private PhQuery<Object> query;
	private long[] qMin;
	private long[] qMax;
	private PhKnnQuery<Object> knnQuery;
	private long[] knnCenter;
	
	private static double AVG_LOW = 0;
	private static int N_LOW = 0;
	private static double AVG_UPP = 0;
	private static int N_UPP = 0;

	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointPHC_IPP(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = PhTree.create(dims);
		//phc = new PhTree16<>(dims);
		qMin = new long[dims];
		qMax = new long[dims];
		knnCenter = new long[dims];
		query = phc.query(qMin, qMax);
		knnQuery = phc.nearestNeighbour(1, knnCenter);
	}
	
	@Override
	public void load(double[] data, int dims) {
		//pp = ExponentPPAnalyzer.analyze(data, dims);
		//pp = new CubePP_KD();
		//pp = new IntegerPP(100L*1000L*1000L);
		pp = new PreProcessorPointF.Multiply(10e10);
		dist = new PhDistance() {
			@Override
			public double dist(long[] v1, long[] v2) {
				double d = 0;
				for (int i = 0; i < v1.length; i++) {
					//we simply cast to double here, should be good enough, 
					double dl = Math.subtractExact(v1[i], v2[i]);
					d += dl*dl;
				}
				return Math.sqrt(d);
			}

			@Override
			public void toMBB(double distance, long[] center, long[] outMin,
					long[] outMax) {
				for (int i = 0; i < center.length; i++) {
					//casting to 'long' always rounds down (floor)
					outMin[i] = (long) (center[i] - distance);
					//casting to 'long' after adding 1.0 always rounds up (ceiling)
					outMax[i] = (long) (center[i] + distance + 1);
				}
			}
		};
		double[] buf = new double[dims];
		int nDuplicates = 0;
		for (int i = 0; i < N; i++) {
			long[] buf2 = new long[dims];
			for (int d = 0; d < dims; d++) {
				buf[d] = data[i*dims+d]; 
			}
			pp.pre(buf, buf2);
			if (phc.put(buf2, O) != null) {
				nDuplicates++;
				//throw new IllegalArgumentException();
			}
		}
		if (nDuplicates > 0) {
			System.err.println("**************************   DUPLICATES FOUND: " + nDuplicates);
		}
		this.data = data;
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		long[][] data = new long[q.length][dims];
		
		for (int i=0; i<q.length; i++) {
			pp.pre(q[i], data[i]);
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
		double[] dd = new double[dims];
		for (int d = 0; d < dims; d++) {
			//val[d] = (long) (data[pos*DIM+d]*pp.PRE);
			dd[d] = data[pos*dims+d];
		}
		pp.pre(dd, val);
		return val;
	}
	
	@Override
	public int query(double[] min, double[] max) {
		if (PhTreeHelper.DEBUG) {
			for (double l: min) {
				AVG_LOW += l;
			}
			for (double l: max) {
				AVG_UPP += l;
			}
			N_LOW += min.length;
			N_UPP += max.length;
		}
		pp.pre(min, qMin);
		pp.pre(max, qMax);
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
		pp.pre(center, knnCenter);
		knnQuery.reset(k, dist, knnCenter);
		double ret = 0;
		double[] v2 = new double[dims];
		while (knnQuery.hasNext()) {
			long[] v = knnQuery.nextEntryReuse().getKey();
			pp.post(v, v2);
			ret += dist(center, v2);
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
		if (PhTreeHelper.DEBUG) {
			System.out.println("QUERY avg: " + AVG_LOW/N_LOW + " / " + AVG_UPP/N_UPP);
			System.out.println("QUERY avg: " + AVG_LOW + "/"+ N_LOW + " / " + AVG_UPP + "/"+ N_UPP);
		}
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
	}

	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		long[] oldL = new long[dims];
		for (int i = 0; i < updateTable.length; ) {
			double[] pOld = updateTable[i++];
			double[] pNew = updateTable[i++];
			long[] newL = new long[dims];
			pp.pre(pOld, oldL);
			pp.pre(pNew, newL);
			if (phc.update(oldL, newL) != null) {
				n++;
			}
		}
		return n;
	}
	
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
