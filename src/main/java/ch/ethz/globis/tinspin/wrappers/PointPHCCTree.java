/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.TestStats.IDX;

public class PointPHCCTree extends Candidate {
	static {
		System.loadLibrary("pht-cpp-benchmark");
	}
	
	private long memoryUsage;
	private final int dims;
	
	private native void nativeSetup(int N, int dims, int depth);
	private native void nativeLoad(double[] data);
	private native void nativePreparePointQuery(double[][] q);
	private native int nativePointQuery(int numPoints, long[] queryPoints);
	private native int nativeRangeQuery(double[] min, double max[], int N, int dims); // TODO: implement on native side!
	private native int nativeUnload();
	private native void nativeRelease();
	private native long nativeMemoryUsage();
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointPHCCTree(TestStats ts) {
		int N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		memoryUsage = nativeMemoryUsage();
		nativeSetup(N, dims, 64);
	}
	
	@Override
	public void load(double[] data, int idxDim) {
		nativeLoad(data);
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		// note: this assumes q.length >= 1!
		long[] data = new long[q.length*q[0].length];
		
		for (int i=0; i<q.length; i++) {
			for (int j=0; j<q[i].length; j++) {
				data[i*q[i].length+j] = BitTools.toSortableLong(q[i][j]);
			}
		}
		
		// currently nothing to be done on the C++ side
		//nativePreparePointQuery(q);
		
		return data;
	}

	@Override
	public int pointQuery(Object qA) {
		int numQueries = ((long[])qA).length/dims;
		return nativePointQuery(numQueries, (long[])qA);
	}

	@Override
	public int unload() {
		return nativeUnload();
	}

	@Override
	public int query(double[] min, double[] max) {
		// TODO: implement!
		throw new RuntimeException("Not yet implemented");
		// TODO: need to implement native counterpart
		// return nativeRangeQuery(qD, N, DIM);
	}
	
	@Override
	public void release() {
		nativeRelease();
	}

	
	/**
	 * Used to test the native code during development process
	 * 
	 * @param args args
	 */
	public static void main(String[] args) {
		int N = 1;
		int dims = 4;
		
		double[] data = {
			0.0d, 0.0d, 0.0d, 0.0d	
		};
		
		long[] pointQueries = new long[data.length];
		for (int i=0; i<data.length; i++)
			pointQueries[i] = BitTools.toSortableLong(data[i]);

		double[] rangeQueryMIN = new double[dims];
		double[] rangeQueryMAX = new double[dims];
		for (int i=0; i<dims; i++) {
			rangeQueryMIN[i] = -Double.MAX_VALUE;
			rangeQueryMAX[i] = Double.MAX_VALUE;
		}
		
		TestStats ts = new TestStats(null, IDX.PHCC, N, dims, false, -1);
		PointPHCCTree tree = new PointPHCCTree(ts);
		
		tree.load(data, dims);
		System.out.println("Point query hits: " + 
		tree.nativePointQuery(data.length / dims, pointQueries));
		System.out.println("Range query hits: " + 
		tree.nativeRangeQuery(rangeQueryMIN, rangeQueryMAX, 0, 0));
		tree.unload();
		
		tree.nativeRelease();
	}
	
	public long getMemoryUsage() {
		long now = nativeMemoryUsage();
		return (now - memoryUsage) * 1024;
	}

	@Override
	public void getStats(TestStats S) {
		// TODO: add pht-cpp statistics collection
		
		// memory usage
		S.statSjvmF = nativeMemoryUsage();
		S.statSjvmE = S.statSjvmF / S.cfgNEntries;
//		//phc.printStats(N);
//		//phc.printQuality();
//		PhTreeQStats q = phc.getQuality();
//		S.statNnodes = q.getNodeCount();
//		S.statSCalc = phc.getStats().size;
//		S.statSMin = phc.getStatsIdealNoNode().size;
//		S.statNpostlen = (long) q.getAvgPostlen(null);
//		S.statNpostHC = q.getPostHcCount();
//		S.statNsubHC = q.getSubHcCount();
//		S.statNBits = phc.getDEPTH();
//		//System.out.println(phc.getQuality());
	}

	@Override
	public int update(double[][] updateTable) {
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
		throw new UnsupportedOperationException();
	}
	
	
	@Override
	public String toString() {
		return "PH-Tree-C++"; 
	}
}
