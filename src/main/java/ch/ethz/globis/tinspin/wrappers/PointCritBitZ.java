/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import org.tinspin.index.critbit.CritBit;
import org.tinspin.index.critbit.CritBitKD;
import org.tinspin.index.critbit.CritBit.QueryIteratorKD;

import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.tinspin.TestStats;

public class PointCritBitZ extends Candidate {

	private CritBitKD<Object> sfc;
	
	private final int dims;
	private final int N;
	
	private double[] data;
	
	private double OFS = 1.0;
	
	public PointCritBitZ(TestStats ts) {
		this.dims = ts.cfgNDims;
		this.N = ts.cfgNEntries;
	}
	
	@Override
	public void load(double[] data, int idxDim) {
		this.data = data;
		
		//calculate offset, because CritBit window queries
		//don't work well with negative values
		double min = 0; 
		for (int i = 0; i < data.length; i++) {
			if (data[i] < min) {
				min = data[i];
			}
		}
		//multiply by 1.1 to ensure that all query rectangles are positive as well
		OFS = Math.abs(min)*1.1;
		
		sfc = CritBit.createKD(64, dims);
		int j = 0;
		Object dummy = new Object();
		long[] val = new long[dims];
		for (int i = 0; i < N; i++) {
			for (int d = 0; d < idxDim; d++) {
				val[d] = BitTools.toSortableLong(data[idxDim*i+d]+OFS); 
			}
			
			// insert new point
			sfc.putKD(val, dummy);
			
			if (++j%N==0)
				System.out.print(j/N+"%, ");
		}
	}
	
	@Override
	public long[][] preparePointQuery(double[][] qA) {
		long[][] r = new long[qA.length][dims];
		for (int i = 0; i < qA.length; i++) {
			for (int d = 0; d < dims; d++) {
				r[i][d] = BitTools.toSortableLong(qA[i][d]+OFS);
			}
		}
		return r;
	}

	@Override
	public int pointQuery(Object qA) {
		long[][] a = (long[][]) qA;
		int n = 0;
		for (long[] q: a) {
			if (sfc.containsKD(q)) {
				n++;
			}
			//log("q=" + Arrays.toString(q));
		}
		return n;
	}

	@Override
	public int query(double[] min, double[] max) {
		long[] lower = new long[dims];
		long[] upper = new long[dims];
		for (int i = 0; i < dims; i++) {
			lower[i] = BitTools.toSortableLong(min[i]+OFS);
			upper[i] = BitTools.toSortableLong(max[i]+OFS);
			if (min[i]+OFS < 0 || max[i]+OFS < 0) {
				throw new IllegalArgumentException(); 
			}
		}
		int n = 0;
		QueryIteratorKD<Object> it = sfc.queryKD(lower, upper);
		while (it.hasNext()) {
			it.nextKey();
			n++;
		}
//		log("N=" + N);
//		log("n=" + n);
//		log("q=" + lower + " / " + upper);
		return n;
	}

	@Override
	public int unload() {
		int n = 0;
		long[] e = new long[dims];
		for (int i = 0; i < N>>1; i++) {
			n += sfc.removeKD(getEntryDPR(e, i)) != null ? 1 : 0;
			n += sfc.removeKD(getEntryDPR(e, N-i-1)) != null ? 1 : 0;
			//n+=2;
		}
		if ((N%2) != 0) {
			int i = (N>>1);
			n += sfc.removeKD(getEntryDPR(e, i)) != null ? 1 : 0;
		}
		return n;
	}

	private long[] getEntryDPR(long[] e, int pos) {
		for (int d = 0; d < dims; d++) {
			e[d] = BitTools.toSortableLong( data[pos*dims+d] + OFS );
		}
		return e;
	}

	
	@Override
	public void release() {
		// nothing to be done
		sfc = null;
	}

	@Override
	public int update(double[][] updateTable) {
		Object dummy = new Object();
		long[] val1 = new long[dims];
		long[] val2 = new long[dims];
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = updateTable[i++];
			for (int d = 0; d < dims; d++) {
				val1[d] = BitTools.toSortableLong(p1[d] + OFS );
			}
			if (sfc.removeKD(val1) != null) {
				for (int d = 0; d < dims; d++) {
					val2[d] = BitTools.toSortableLong(p2[d] + OFS );
				}
				sfc.putKD(val2, dummy);
				n++;
			}
		}
		return n;
	}
	
	@Override
	public String toString() {
		return "CritBitKD;OFS=" + OFS + ";";
	}
}
