/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;

import org.zoodb.index.Index;
import org.zoodb.index.rtree.RTree;
import org.zoodb.index.rtree.RTreeIterator;
import org.zoodb.index.rtree.RTreeIteratorKnnRS;

import ch.ethz.globis.tinspin.TestStats;

public class PointRStarZ extends Candidate {
	
	private final RTree<double[]> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private RTreeIterator<double[]> it;
	private RTreeIteratorKnnRS<double[]> itKnn;

	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointRStarZ(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = RTree.createRStar(dims);
	}
	
	@Override
	public void load(double[] data, int dims) {
		for (int i = 0; i < N; i++) {
			double[] buf = new double[dims];
			for (int d = 0; d < dims; d++) {
				buf[d] = data[i*dims+d]; 
			}
			phc.insert(buf, buf);
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
			if (phc.queryExact(q, q) != null) {
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
		if (it == null) {
			it = phc.queryIntersect(min, max);
		} else {
			it.reset(min, max);
		}
		int n = 0;
		while (it.hasNext()) {
			it.next();
			n++;
		}
//		int n = ((PhTree7)phc).queryAll(min2, max2).size();
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		if (k == 1) {
			return phc.query1NN(center).dist();
		}
		if (itKnn == null) {
			itKnn = phc.queryKNN(center, k, null);
		} else {
			itKnn.reset(center, k, null);
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
	public Index<double[]> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats s) {
		s.statNnodes = phc.getNodeCount();
		s.statNpostlen = phc.getDepth();
		//phc.printStats(N);
		//phc.printQuality();
		//PhTreeStats q = phc.getStats();
		//S.setStats(q);
		//System.out.println(phc.getQuality());
	}
	
	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = Arrays.copyOf(updateTable[i++], dims);
			if (phc.update(p1, p1, p2, p2) != null) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public boolean supportsPointQuery() {
		return dims <= 16;
	}
	
	@Override
	public boolean supportsUpdate() {
		return dims <= 16;
	}

	@Override
	public boolean supportsUnload() {
		return dims <= 16;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
