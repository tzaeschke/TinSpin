/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.List;

import ch.ethz.globis.phtree.PhTreeSolidF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhEntryDistSF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhEntrySF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhKnnQuerySF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhQuerySF;
import ch.ethz.globis.phtree.pre.PreProcessorRangeF;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

/**
 * Rectangle PH-Tree that uses the PH-Tree solid/rectangle-float API. 
 */
public class RectanglePHCF extends CandidatePHC {
	
	private final PhTreeSolidF<Integer> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private final PreProcessorRangeF pre;
	private PhQuerySF<Integer> query = null;
	private PhKnnQuerySF<Integer> knnQuery;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public RectanglePHCF(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		this.pre = new PreProcessorRangeF.IEEE(dims);
		this.phc = PhTreeSolidF.create(dims, pre);
	}
	
	@Override
	public void load(double[] data, int dims) {
		double[] lo = new double[dims];
		double[] hi = new double[dims];
		int d = 0;
		int pos = 0;
		for (int n = 0; n < N; n++) {
			System.arraycopy(data, pos, lo, 0, dims);
			pos += dims;
			System.arraycopy(data, pos, hi, 0, dims);
			pos += dims;
			if (phc.put(lo, hi, n) != null) {
				d++;
				n--;
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
	public int pointQuery(Object qA, int[] ids) {
		int n = 0;
		double[][] dA = (double[][]) qA; 
		for (int i = 0; i < dA.length; i+=2) {
			if (phc.contains(dA[i], dA[i+1])) {
				n++;
			}
		}
		return n;
	}

	@Override
	public int unload() {
		int n = 0;
		double[] lo = new double[dims];
		double[] hi = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			System.arraycopy(data, i*dims*2, lo, 0, dims);
			System.arraycopy(data, i*dims*2+dims, hi, 0, dims);
			n += phc.remove(lo, hi) != null ? 1 : 0;
			int i2 = N-i-1;
			System.arraycopy(data, i2*dims*2, lo, 0, dims);
			System.arraycopy(data, i2*dims*2+dims, hi, 0, dims);
			n += phc.remove(lo, hi) != null? 1 : 0;
		}
		if ((N%2) != 0) {
			int i = (N>>1);
			System.arraycopy(data, i*dims*2, lo, 0, dims);
			System.arraycopy(data, i*dims*2+dims, hi, 0, dims);
			n += phc.remove(lo, hi) != null ? 1 : 0;
		}
		return n;
	}
	
	
	@Override
	public int query(double[] min, double[] max) {
		if (query == null) {
			query = phc.queryIntersect(min, max);
		} else {
			query.reset(min, max);
		}
		int n = 0;
		while (query.hasNext()) {
			query.next();
			n++;
		}
		return n;
	}
	
	@Override
	public List<PhEntrySF<Integer>> queryToList(double[] min, double[] max) {
		return phc.queryIntersectAll(min, max);
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		if (knnQuery == null) {
			knnQuery = phc.nearestNeighbour(k, null, center);
		} else {
			knnQuery.reset(k, null, center);
		}
		double ret = 0;
		int n = 0;
		while (knnQuery.hasNext()) {
			PhEntryDistSF<Integer> e = knnQuery.nextEntryReuse();
			ret += e.dist();
			//ret += distREdge(center, e.lower(), e.upper());
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
	public PhTreeSolidF<Integer> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		PhTreeStats q = phc.getInternalTree().getStats();
		setStats(S, q);
		System.out.println(q.toStringHist());
	}
	


	@Override
	public int update(double[][] updateTable, int[] ids) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] lo1 = updateTable[i++];
			double[] up1 = updateTable[i++];
			double[] lo2 = updateTable[i++];
			double[] up2 = updateTable[i++];
			if (phc.update(lo1, up1, lo2, up2) != null) {
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
