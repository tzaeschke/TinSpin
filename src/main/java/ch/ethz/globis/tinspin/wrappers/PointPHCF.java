/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.phtree.PhDistanceF;
import ch.ethz.globis.phtree.PhTreeF;
import ch.ethz.globis.phtree.PhTreeF.PhKnnQueryF;
import ch.ethz.globis.phtree.PhTreeF.PhQueryF;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

public class PointPHCF extends CandidatePHC {
	
	private final PhTreeF<Integer> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private PhKnnQueryF<Integer> knnQuery;
    private PhQueryF<Integer> pit;
    
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointPHCF(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = PhTreeF.create(dims);
		knnQuery = phc.nearestNeighbour(1, new double[dims]);
	}
	
	@Override
	public void load(double[] data, int dims) {
		double[] buf = new double[dims];
		for (int i = 0; i < N; i++) {
			for (int d = 0; d < dims; d++) {
				buf[d] = data[i*dims+d]; 
			}
			if (phc.put(buf, i) != null) {
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
	public int pointQuery(Object qA, int[] ids) {
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
		if (pit == null) {
			pit = phc.query(min, max);
		} else {
			pit.reset(min, max);
		}
		int n = 0;
		while (pit.hasNext()) {
			pit.next();
			n++;
		}
//		int n = ((PhTree7)phc).queryAll(min2, max2).size();
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	@Override
	public void release() {
		data = null;
	}

	@Override
	public double knnQuery(int k, double[] center) {
		knnQuery.reset(k, PhDistanceF.THIS, center);
		double ret = 0;
		int n = 0;
		while (knnQuery.hasNext() && n++ < k) {
			double[] v = knnQuery.nextEntryReuse().getKey();
			ret += dist(center, v);
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
	
	/**
	 * Used to test the native code during development process
	 */
	@Override
	public PhTreeF<Integer> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		//phc.printStats(N);
		//phc.printQuality();
		PhTreeStats q = phc.getInternalTree().getStats();
		setStats(S, q);
		//System.out.println(phc.getQuality());
	}
	
	@Override
	public int update(double[][] updateTable, int[] ids) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = updateTable[i++];
			if (phc.update(p1, p2) != null) {
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
