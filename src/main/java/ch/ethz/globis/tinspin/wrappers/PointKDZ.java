/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;

import org.tinspin.index.Index;
import org.tinspin.index.kdtree.KDTree;
import org.tinspin.index.kdtree.KDTree.KDStats;

import ch.ethz.globis.tinspin.TestStats;
/**
 * KD-Tree.
 * 
 * @author Tilmann Zäschke
 *
 */
public class PointKDZ extends Candidate {
	
	private KDTree<Integer> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private Index.PointIteratorKnn<Integer> itKnn;
	private Index.PointIterator<Integer> pit;

	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointKDZ(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
	}
	
	@Override
	public void load(double[] data, int dims) {
		phc = KDTree.create(dims);

		for (int i = 0; i < N; i++) {
			double[] buf = new double[dims];
			for (int d = 0; d < dims; d++) {
				buf[d] = data[i*dims+d]; 
			}
			phc.insert(buf, i);
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
			if (phc.containsExact(q)) {
				n++;
			}
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
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		if (k == 1) {
			return phc.query1nn(center).dist();
		}
		if (itKnn == null) {
			itKnn = phc.queryKnn(center, k);
		} else {
			itKnn.reset(center, k);
		}
		double ret = 0;
		while (itKnn.hasNext()) {
			ret += itKnn.next().dist();
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
	public KDTree<Integer> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats s) {
		KDStats qs = phc.getStats();
		s.statNnodes = qs.getNodeCount();
		s.statNpostlen = qs.getMaxDepth();
		s.statNDistCalc = qs.getNDistCalc();
		s.statNDistCalc1NN = qs.getNDistCalc1NN();
		s.statNDistCalcKNN = qs.getNDistCalcKNN();
	}
	
	@Override
	public int update(double[][] updateTable, int[] ids) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = Arrays.copyOf(updateTable[i++], dims);
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
