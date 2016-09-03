/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.List;

import ch.ethz.globis.phtree.PhTreeSolid;
import ch.ethz.globis.phtree.PhTreeSolid.PhQueryS;
import ch.ethz.globis.phtree.PhTreeSolidF.PhEntrySF;
import ch.ethz.globis.phtree.pre.PreProcessorR2RF;
import ch.ethz.globis.phtree.pre.R2RF_IntegerPP;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

public class RectanglePHCRect extends Candidate {
	
	private final PhTreeSolid<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private static final Object O = new Object();
	private PreProcessorR2RF pp;
	private PhQueryS<Object> it = null;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public RectanglePHCRect(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = PhTreeSolid.create(dims);
		//this.pp = new R2RF_IEEE(); 
		this.pp = new R2RF_IntegerPP(10e10);
	}
	
	@Override
	public void load(double[] data, int dims) {
		long[] bufLo = new long[dims];
		long[] bufHi = new long[dims];
		int d = 0;
		int pos = 0;
		for (int n = 0; n < N; n++) {
			pp.pre(data, pos, pos+dims, bufLo, bufHi);
			pos += dims;
			pos += dims;
			if (phc.put(bufLo, bufHi, O) != null) {
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
		long[] bufLo = new long[dims];
		long[] bufHi = new long[dims];
		for (int i = 0; i < dA.length; i+=2) {
			pp.pre(dA[i], dA[i+1], bufLo, bufHi);
			if (phc.contains(bufLo, bufHi)) {
				n++;
			}
			//log("q=" + Arrays.toString(q));
		}
		return n;
	}

	@Override
	public int unload() {
		int n = 0;
		long[] bufLo = new long[dims];
		long[] bufHi = new long[dims];
		for (int i = 0; i < N>>1; i++) {
			pp.pre(data, i*dims*2, i*dims*2+dims, bufLo, bufHi);
			n += phc.remove(bufLo, bufHi) != null ? 1 : 0;
			int i2 = N-i-1;
			pp.pre(data, i2*dims*2, i2*dims*2+dims, bufLo, bufHi);
			n += phc.remove(bufLo, bufHi) != null? 1 : 0;
		}
		return n;
	}
	
	@Override
	public int query(double[] min, double[] max) {
		long[] bufLo = new long[dims];
		long[] bufHi = new long[dims];
		pp.pre(min, max, bufLo, bufHi);
		if (it == null) {
			it = phc.queryIntersect(bufLo, bufHi);
		} else {
			it.reset(bufLo, bufHi);
		}
		int n = 0;
		while (it.hasNext()) {
			it.next();
			n++;
		}
//		int n = phc.queryIntersectAll(min, max).size();
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	@Override
	public List<PhEntrySF<Object>> queryToList(double[] min, double[] max) {
		throw new UnsupportedOperationException();
		//return phc.queryIntersectAll(min, max);
	}
	
	@Override
	public void release() {
		data = null;
	}

	public PhTreeSolid<Object> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		//phc.printStats(N);
		//phc.printQuality();
		PhTreeStats q = phc.getStats();
		S.setStats(q);
		//System.out.println(phc.getQuality());
		System.out.println(q.toStringHist());
	}
	


	@Override
	public int update(double[][] updateTable) {
		long[] bufLo1 = new long[dims];
		long[] bufHi1 = new long[dims];
		long[] bufLo2 = new long[dims];
		long[] bufHi2 = new long[dims];
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] lo1 = updateTable[i++];
			double[] up1 = updateTable[i++];
			double[] lo2 = updateTable[i++];
			double[] up2 = updateTable[i++];
			pp.pre(lo1, up1, bufLo1, bufHi1);
			pp.pre(lo2, up2, bufLo2, bufHi2);
			if (phc.update(bufLo1, bufHi1, bufLo2, bufHi2) != null) {
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
