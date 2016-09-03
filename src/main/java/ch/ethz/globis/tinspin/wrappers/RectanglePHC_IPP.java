/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.List;

import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTreeSolidF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhEntrySF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhQuerySF;
import ch.ethz.globis.phtree.pre.PreProcessorRangeF;
import ch.ethz.globis.phtree.util.PhMapperKey;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

public class RectanglePHC_IPP extends Candidate {
	
	private final PhTreeSolidF<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private static final Object O = new Object();

	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public RectanglePHC_IPP(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		PreProcessorRangeF pre = new PreProcessorRangeF.Multiply(dims, 1e9); //1..1000
		//PreProcessorRangeF pre = new EmptyPPRF();  //6e-6
		//PreProcessorRangeF pre = new ShiftMulIPP(1e12, 1.0);  //6e-6
		//PreProcessorRangeF pre = new ShiftIPP(1.0);  //6e-6
		phc = PhTreeSolidF.create(dims, pre);
	}
	
	@Override
	public void load(double[] data, int dims) {
		double[] lo = new double[dims];
		double[] hi = new double[dims];
		int d = 0;
		int pos = 0;
		if (dims < 7) {
			for (int n = 0; n < N; n++) {
				for (int dd = 0; dd < dims; dd++) {
					lo[dd] = data[pos++];
				}
				for (int dd = 0; dd < dims; dd++) {
					hi[dd] = data[pos++];
				}
				if (phc.put(lo, hi, O) != null) {
					d++;
					n--;
					//throw new IllegalArgumentException();
				}
			}
		} else {
			for (int n = 0; n < N; n++) {
				System.arraycopy(data, pos, lo, 0, dims);
				pos += dims;
				System.arraycopy(data, pos, hi, 0, dims);
				pos += dims;
				if (phc.put(lo, hi, O) != null) {
					d++;
					n--;
					//throw new IllegalArgumentException();
				}
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
			if (phc.contains(dA[i], dA[i+1])) {
				n++;
			}
			//log("q=" + Arrays.toString(q));
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
			n += phc.remove(lo, hi) != null ? 1 : 0;
		}
		return n;
	}
	
	private static final int MAX = Integer.MAX_VALUE;
	private static final PhMapperKey<double[]> MAPPER = PhMapperKey.DOUBLE_ARRAY();
	
	private PhQuerySF<Object> it = null;
	
	@Override
	public int query(double[] min, double[] max) {
		if (it == null) {
			it = phc.queryIntersect(min, max);
		} else {
			it.reset(min, max);
		}
		int n = 0;
		while (it.hasNext()) {
			n += it.nextEntryReuse().lower()[0] != Double.NaN ? 1 : 0;
		}
//		int n = phc.queryIntersectAll(min, max, MAX, FILTER, MAPPER).size();
//		int n = phc.queryIntersectAll(min, max, MAX, null, null).size();

//		List<PHREntry> all = phc.queryIntersectAll(min, max);
//		int n = all.size();
		
		//log("q=" + Arrays.toString(q));
		//System.out.println("n1=" + n1 + "  n2="+ n);
		return n;
	}
	
	@Override
	public List<PhEntrySF<Object>> queryToList(double[] min, double[] max) {
//		List<PHREntry> all = new ArrayList<>();
//		Iterator<PHREntry> it = phc.queryIntersect(min, max);
//		while (it.hasNext()) {
//			all.add(it.next());
//		}
//		int n = phc.queryIntersectAll(min, max, MAX, FILTER, MAPPER).size();
//		int n = phc.queryIntersectAll(min, max, MAX, null, null).size();
		
		List<PhEntrySF<Object>> all = phc.queryIntersectAll(min, max);
		return all;
	}
	
	@Override
	public void release() {
		data = null;
	}

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
