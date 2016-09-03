/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Iterator;

import ch.ethz.globis.phtree.nv.PhTreeNV;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestRunner;
import ch.ethz.globis.tinspin.TestStats;

public class PointPHC_v1 extends Candidate {
	
	private final PhTreeNV phc;
	private final int dims;
	private final int N;
	private double[] data;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointPHC_v1(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = PhTreeNV.createV1(dims, 64);
	}
	
	@Override
	public void load(double[] data, int dims) {
		long[] buf = new long[dims];
		for (int i = 0; i < N; i++) {
			for (int d = 0; d < dims; d++) {
				buf[d] = f2l(data[i*dims+d]); 
			}
			if (phc.insert(buf)) {
				throw new IllegalArgumentException();
			}
		}
		this.data = data;
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		long[][] data = new long[q.length][dims];
		
		for (int i=0; i<q.length; i++) {
			f2l(q[i], data[i]);
		}
		return data;
	}

	@Override
	public int pointQuery(Object qA) {
		int n = 0;
		for (long[] q: (long[][])qA) {
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
		long[] l = new long[dims];
		for (int i = 0; i < N>>1; i++) {
			n += phc.delete(getEntry(l, i)) ? 1 : 0;
			n += phc.delete(getEntry(l, N-i-1)) ? 1 : 0;
		}
		return n;
	}

	private long[] getEntry(long[] val, int pos) {
		for (int d = 0; d < dims; d++) {
			val[d] = f2l(data[pos*dims+d]);
		}
		return val;
	}
	
	@Override
	public int query(double[] min, double[] max) {
		long[] min2 = new long[dims];
		long[] max2 = new long[dims];
		f2l(min, min2);
		f2l(max, max2);
		Iterator<?> it = phc.query(min2, max2);
		int n = 0;
		while (it.hasNext()) {
			it.next();
			n++;
		}
//		List<long[]> it = phc.queryAll(min2, max2);
//		int n = it.size();
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	@Override
	public void release() {
		data = null;
	}

	
	/**
	 * Used to test the native code during development process
	 */
	public PhTreeNV getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		//phc.printStats(N);
		//phc.printQuality();
		PhTreeStats q = phc.getQuality();
		S.setStats(q);
		//System.out.println(phc.getQuality());
		System.out.println(q.toStringHist());
	}
	
	@Override
	public int update(double[][] updateTable) {
		long[] oldKey = new long[dims];
		long[] newKey = new long[dims];
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] pOld = updateTable[i++];
			double[] pNew = updateTable[i++];
			for (int d = 0; d < dims; d++) {
				oldKey[d] = f2l(pOld[d]); 
				newKey[d] = f2l(pNew[d]); 
			}
			if (phc.delete(oldKey) && !phc.insert(newKey)) {
				n++;
			} else {
				//throw new IllegalStateException("i=" + i + " old=" + Arrays.toString(pOld) + 
				//		" / " + Arrays.toString(pNew));
			}
		}
		return n;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
