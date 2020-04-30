/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.phtree.PhDistanceMMF;
import ch.ethz.globis.phtree.PhTreeMultiMapF;
import ch.ethz.globis.phtree.PhTreeMultiMapF.PhKnnQueryMMF;
import ch.ethz.globis.phtree.PhTreeMultiMapF.PhQueryMMF;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

public class PointPHCMMF extends CandidatePHC {
	
	private final PhTreeMultiMapF<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private PhKnnQueryMMF<Object> knnQuery;
    private PhQueryMMF<Object> pit;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointPHCMMF(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = PhTreeMultiMapF.create(dims);
		knnQuery = phc.nearestNeighbour(1, new double[dims]);
	}
	
	@Override
	public void load(double[] data, int dims) {
		Object O = new Object();
		double[] buf = new double[dims];
		for (int i = 0; i < N; i++) {
			for (int d = 0; d < dims; d++) {
				buf[d] = data[i*dims+d]; 
			}
			if (phc.put(buf, i, O) != null) {
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
            // TODO This does not work
			if (phc.contains(q, -1)) {
				n++;
			}
			//log("q=" + Arrays.toString(q));
		}
		// TODO return n;
		return -((double[][])qA).length;
	}

	@Override
	public int unload() {
		int n = 0;
		double[] l = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			n += phc.remove(getEntry(l, i), i) != null ? 1 : 0;
			n += phc.remove(getEntry(l, N-i-1), N-i-1) != null ? 1 : 0;
		}
		if ((N%2) != 0) {
			int i = (N>>1);
			n += phc.remove(getEntry(l, i), i) != null ? 1 : 0;
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
		return n;
	}
	
	@Override
	public void release() {
		data = null;
	}

	@Override
	public double knnQuery(int k, double[] center) {
		knnQuery.reset(k, PhDistanceMMF.THIS, center);
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
	public PhTreeMultiMapF<Object> getNative() {
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
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = updateTable[i++];
			// TODO This does not work
			if (phc.update(p1, -1, p2) != null) {
				n++;
			}
		}
        // TODO return n;
		return -updateTable.length;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
