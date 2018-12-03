/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;
import java.util.List;

import ch.ethz.globis.phtree.PhDistanceSF;
import ch.ethz.globis.phtree.PhDistanceSFEdgeDist;
import ch.ethz.globis.phtree.PhEntryDist;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTree.PhKnnQuery;
import ch.ethz.globis.phtree.PhTree.PhQuery;
import ch.ethz.globis.phtree.PhTreeSolidF.PhEntrySF;
import ch.ethz.globis.phtree.pre.PreProcessorRangeF;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

public class RectanglePHC extends Candidate {
	
	private final PhTree<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private static final Object O = new Object();
	private final long[] buffer;
	private final long[] bufLow;
	private final long[] bufUpp;
	private final double[] qMIN;
	private final double[] qMAX;
	private final PreProcessorRangeF pre;
	private PhQuery<Object> q = null;
	private PhKnnQuery<Object> qKNN = null;
	private final PhDistanceSF distFn;
	
	/**
	 * Setup of a native PH tree
	 * @param ts test stats
	 */
	public RectanglePHC(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		this.phc = PhTree.create(2*dims);
		this.pre = new PreProcessorRangeF.IEEE(dims);
		//this.pre = new PreProcessorRangeF.IPP(dims, 10e9);
		//distFn = new PhDistanceSFCenterDist(pre, dims);
		distFn = new PhDistanceSFEdgeDist(pre, dims);
		this.buffer = new long[2*dims];
		this.bufLow = new long[2*dims];
		this.bufUpp = new long[2*dims];
		this.qMIN = new double[dims];
		Arrays.fill(qMIN, Double.NEGATIVE_INFINITY);
		this.qMAX = new double[dims];
		Arrays.fill(qMAX, Double.POSITIVE_INFINITY);
	}
	
	@Override
	public void load(double[] data, int dims) {
		long[] lVal = new long[dims*2];
		int d = 0;
		int pos = 0;
		for (int n = 0; n < N; n++) {
			pre.pre(data, pos, data, pos+dims, lVal);
			pos += 2*dims;
			if (phc.put(lVal, O) != null) {
				d++;
				n--;
				//throw new IllegalArgumentException();
			}
		}
		if (d > 0) {
			System.err.println("Doublets found: " + d);
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
			pre.pre(dA[i], dA[i+1], buffer);
			if (phc.contains(buffer)) {
				n++;
			}
			//log("q=" + Arrays.toString(q));
		}
		return n;
	}

	@Override
	public int unload() {
		int n = 0;
		for (int i = 0; i < N>>1; i++) {
//			System.arraycopy(data, i*dims*2, lo, 0, dims);
//			System.arraycopy(data, i*dims*2+dims, hi, 0, dims);
			pre.pre(data, i*dims*2, data, i*dims*2+dims, buffer);
			n += phc.remove(buffer) != null ? 1 : 0;
			int i2 = N-i-1;
//			System.arraycopy(data, i2*dims*2, lo, 0, dims);
//			System.arraycopy(data, i2*dims*2+dims, hi, 0, dims);
			pre.pre(data, i2*dims*2, data, i2*dims*2+dims, buffer);
			n += phc.remove(buffer) != null? 1 : 0;
		}
		if ((N%2) != 0) {
			int i = (N>>1);
			pre.pre(data, i*dims*2, data, i*dims*2+dims, buffer);
			n += phc.remove(buffer) != null ? 1 : 0;
		}
		return n;
	}
	
	
	@Override
	public int query(double[] min, double[] max) {
		pre.pre(qMIN, min, bufLow);
		pre.pre(max, qMAX, bufUpp);
		if (q == null) {
			q = phc.query(bufLow, bufUpp);
		} else {
			q.reset(bufLow, bufUpp);
		}
		int n = 0;
		while (q.hasNext()) {
			q.next();
			n++;
		}
		return n;
	}
	
	@Override
	public List<PhEntrySF<Object>> queryToList(double[] min, double[] max) {
		throw new UnsupportedOperationException();
		//return phc.queryIntersectAll(min, max);
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		pre.pre(center, center, buffer);
		if (qKNN == null) {
			qKNN = phc.nearestNeighbour(k, distFn, null, buffer);
		} else {
			qKNN.reset(k, distFn, buffer);
		}
		double ret = 0;
		int n = 0;
		while (qKNN.hasNext()) {
			PhEntryDist<Object> e = qKNN.nextEntryReuse();
			ret += e.dist();
			if (++n == k) {
				break;
			}
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

	@Override
	public PhTree<Object> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		PhTreeStats q = phc.getStats();
		S.setStats(q);
		System.out.println(q.toStringHist());
	}
	


	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] lo1 = updateTable[i++];
			double[] up1 = updateTable[i++];
			double[] lo2 = updateTable[i++];
			double[] up2 = updateTable[i++];
			//low/upp refer to old/new
			pre.pre(lo1, up1, bufLow);
			pre.pre(lo2, up2, bufUpp);
			if (phc.update(bufLow, bufUpp) != null) {
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
