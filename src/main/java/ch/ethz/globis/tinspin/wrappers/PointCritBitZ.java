/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import org.zoodb.index.critbit.CritBit;
import org.zoodb.index.critbit.CritBit.QueryIteratorKD;
import org.zoodb.index.critbit.CritBitKD;

import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.tinspin.wrappers.Candidate;

public class PointCritBitZ extends Candidate {

	private CritBitKD<Object> sfc;
	
	private final int dims;
	private final int N;
	
	private double[] data;
	
	private PointCritBitZ(int dims, int N) {
		this.dims = dims;
		this.N = N;
	}
	
	public static PointCritBitZ create(int dims, int N) {
		return new PointCritBitZ(dims, N);
	}
	
	@Override
	public void load(double[] data, int idxDim) {
		this.data = data;
		sfc = CritBit.createKD(64, dims);
		int j = 0;
		Object dummy = new Object();
		long[] val = new long[dims];
		for (int i = 0; i < N; i++) {
			for (int d = 0; d < idxDim; d++) {
				val[d] = BitTools.toSortableLong(data[idxDim*i+d]); 
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
			BitTools.toSortableLong(qA[i], r[i]);
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
			lower[i] = BitTools.toSortableLong(min[i]);
			upper[i] = BitTools.toSortableLong(max[i]);
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
		return n;
	}

	private long[] getEntryDPR(long[] e, int pos) {
		for (int d = 0; d < dims; d++) {
			e[d] = BitTools.toSortableLong( data[pos*dims+d] );
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
			BitTools.toSortableLong(p1, val1);
			if (sfc.removeKD(val1) != null) {
				BitTools.toSortableLong(p2, val2);
				sfc.putKD(val2, dummy);
				n++;
			}
		}
		return n;
	}
}
