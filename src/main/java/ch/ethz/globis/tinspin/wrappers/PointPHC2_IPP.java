/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.phtree.PhDistance;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTreeF;
import ch.ethz.globis.phtree.PhTreeF.PhKnnQueryF;
import ch.ethz.globis.phtree.PhTreeF.PhQueryF;
import ch.ethz.globis.phtree.pre.PreProcessorPointF;
import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

public class PointPHC2_IPP extends Candidate {
	
	private static final Object O = new Object();
	
	private PhTreeF<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private PhQueryF<Object> query;
	private PhKnnQueryF<Object> knnQuery;
	private PreProcessorPointF pp;
	private PhDistance dist;
	
	private static final double POWER = 0.01;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointPHC2_IPP(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		//PreProcessorPointF pp = new CubePP_KD();
		//PreProcessorPointF pp = ExponentPPAnalyzer.analyze(data, dims);
		pp = new PreProcessorPointF() {
			private final double p = POWER;
//			public Multiply(double multiplyer) {
//				preMult = multiplyer;
//				postMult = 1./multiplyer;
//			}
			
			@Override
			public void pre(double[] raw, long[] pre) {
				for (int d=0; d<raw.length; d++) {
					pre[d] = BitTools.toSortableLong(Math.pow(raw[d], p));
				}
			}

			@Override
			public void post(long[] pre, double[] post) {
				for (int d=0; d<pre.length; d++) {
					post[d] = Math.pow(BitTools.toDouble(pre[d]), 1/p);
				}
			}
		};
		dist = new PhDistance() {
			private final double p = POWER;

			@Override
			public double dist(long[] v1, long[] v2) {
				double d = 0;
				for (int i = 0; i < v1.length; i++) {
					double dl = Math.pow(BitTools.toDouble(v1[i]), 1/p) - 
							Math.pow(BitTools.toDouble(v2[i]), 1/p);
					d += dl*dl;
				}
				return Math.sqrt(d);
			}

			@Override
			public void toMBB(double distance, long[] center, long[] outMin,
					long[] outMax) {
				for (int i = 0; i < center.length; i++) {
					double c = Math.pow(BitTools.toDouble(center[i]), 1/p);
					outMin[i] = BitTools.toSortableLong(Math.pow(c - distance, p));
					outMax[i] = BitTools.toSortableLong(Math.pow(c + distance, p));
				}
			}};
		phc = PhTreeF.create(dims, pp);
		query = phc.query(new double[dims], new double[dims]);
		knnQuery = phc.nearestNeighbour(1, new double[dims]);
	}
	
	@Override
	public void load(double[] data, int dims) {
		for (int i = 0; i < N; i++) {
			double[] buf = new double[dims];
			for (int d = 0; d < dims; d++) {
				buf[d] = data[i*dims+d]; 
			}
			if (phc.put(buf, O) != null) {
				throw new IllegalArgumentException();
			}
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
		for (double[] q: (double[][])qA) {
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
		double[] l = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
			n += phc.remove(getEntry(l, N-i-1)) != null ? 1 : 0;
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
		query.reset(min, max);
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
		knnQuery.reset(k, dist, center);
		double ret = 0;
		while (knnQuery.hasNext()) {
			double[] v = knnQuery.nextEntryReuse().getKey();
			ret += dist(center, v);
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
	public PhTree<Object> getNative() {
		return phc.getInternalTree();
	}

	@Override
	public void getStats(TestStats S) {
		//phc.printStats(N);
		//phc.printQuality();
		PhTreeStats q = phc.getInternalTree().getStats();
		S.setStats(q);
		//System.out.println(phc.getQuality());
		System.out.println(q.toStringHist());
	}

	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] pOld = updateTable[i++];
			double[] pNew = updateTable[i++];
			if (phc.update(pOld, pNew) != null) {
				n++;
			} else {
				//throw new IllegalStateException("i=" + i + " old=" + Arrays.toString(pOld) + 
				//		" / " + Arrays.toString(pNew));
			}
		}
		return n;
//		Object dummy = new Object();
//		int n = 0;
//		for (int i = 0; i < updateTable.length; ) {
//			int[] p1 = toKey(updateTable[i++]);
//			int[] p2 = toKey(updateTable[i++]);
//			if (cbf.remove(p1) != null) {
//				cbf.put(p2, dummy);
//				n++;
//			}
//		}
//		return n;
//		throw new UnsupportedOperationException();
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
	
}
