/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Iterator;

import ch.ethz.globis.phtree.PhEntry;
import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.phtree.v5b.PhTree5b;
import ch.ethz.globis.tinspin.TestStats;

public class PointPHC_PHE extends Candidate {
	
	private final PhTree<PhEntry<?>> phc;
	private final int dims;
	private final int N;
	private double[] data;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointPHC_PHE(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		phc = new PhTree5b<>(dims, 64);
//		this.pre = new PreProcessorPoint() {
//			@Override
//			public void pre(double[] raw, long[] pre) {
//				for (int d=0; d<raw.length; d++) {
//					pre[d] = BitTools.toSortableLong(raw[d]);
//				}
//			}
//			
//			@Override
//			public void post(long[] pre, double[] post) {
//				for (int d=0; d<pre.length; d++) {
//					post[d] = BitTools.toDouble(pre[d]);
//				}
//			}
//		};
	}

	private final void pre(double[] f, long[] l) {
		for (int i = 0; i < f.length; i++) {
			l[i] = pre(f[i]);
		}
	}

	
	private final long pre(double d) {
		return BitTools.toSortableLong(d);
		//return (long) (d*10*1000*1000);
	}
	
//	private final double post(long l) {
//		return BitTools.toDouble(l);
//	}
	
	@Override
	public void load(double[] data, int dims) {


//		IntStream.range(0, N).mapToObj(
//				i -> {
//					long[] arr = new long[DIM];
//					for (int j = 0; j < DIM; j++) {
//						arr[j] = BitTools.toSortableLong(data[DIM * i + j]);
//					}
//					return arr;
//				}).forEach((buf)->{
//					PVEntry<?> e = new PVEntry<Object>(buf, null);
//					if (phc.put(buf, e) != null) {
//						throw new IllegalArgumentException();
//					}
//				});

//		Arrays.stream(data).mapToObj(new DataToLong()).filter((x)-> x != null).forEach((buf)->{
//			PVEntry<?> e = new PVEntry<Object>(buf, null);
//			if (phc.put(buf, e) != null) {
//				throw new IllegalArgumentException();
//			}
//		});
		
//		IntStream.range(0, N)
//	     // for each int i, derive a DoubleStream that maps each
//	     // j in range(0, k) to the double in data[k * i + j]
//	     .mapToObj(
//	       i -> IntStream.range(0, DIM)
//	           .mapToDouble(j -> data[DIM * i + j])
//	     )
//	     // flatMap from Stream<DoubleStream> to DoubleStream 
//	     .flatMapToDouble(Function.identity())
//	     // call BitTools::toSortableLong on every double
//	     .mapToLong(BitTools::toSortableLong)
//	     // collect into a long[]
//	     .toArray()
//	   ;
		


//		IntStream.range(0, N).mapToObj(
//				i -> {
//					long[] arr = new long[DIM];
//					for (int j = 0; j < DIM; j++) {
//						arr[j] = pre(data[DIM * i + j]);
//					}
//					return arr;
//				}).forEach( buf -> {
//					phc.put(buf, new PVEntry<Object>(buf, null));
////					PVEntry<?> e = new PVEntry<Object>(buf, null);
////					if (phc.put(buf, e) != null) {
////						throw new IllegalArgumentException();
////					}
//				});


//		IntStream.range(0, N)
//		.mapToObj(i -> IntStream.range(0, DIM)
//			.mapToLong(j -> pre(data[DIM * i + j])).toArray()
//		).forEach(buf -> phc.put(buf, new PVEntry<Object>(buf, null)));

		
		for (int i = 0; i < N; i++) {
			long[] buf = new long[dims];
			for (int d = 0; d < dims; d++) {
				buf[d] = pre(data[i*dims+d]); 
			}
			phc.put(buf, new PhEntry<Object>(buf, null));
//			PVEntry<?> e = new PVEntry<Object>(buf, null);
//			if (phc.put(buf, e) != null) {
//				throw new IllegalArgumentException();
//			}
		}
		this.data = data;
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		long[][] data = new long[q.length][dims];
		
		for (int i=0; i<q.length; i++) {
			pre(q[i], data[i]);
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
			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
			n += phc.remove(getEntry(l, N-i-1)) != null ? 1 : 0;
		}
		return n;
	}

	private long[] getEntry(long[] val, int pos) {
		for (int d = 0; d < dims; d++) {
			val[d] = pre(data[pos*dims+d]);
		}
		return val;
	}
	
	@Override
	public int query(double[] min, double[] max) {
		long[] min2 = new long[dims];
		long[] max2 = new long[dims];
		pre(min, min2);
		pre(max, max2);
		Iterator<PhEntry<?>> it = phc.query(min2, max2);
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
	public PhTree<?> getNative() {
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
		long[] oldKey = new long[dims];
		long[] newKey = new long[dims];
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] pOld = updateTable[i++];
			double[] pNew = updateTable[i++];
			for (int d = 0; d < dims; d++) {
				oldKey[d] = pre(pOld[d]); 
				newKey[d] = pre(pNew[d]); 
			}
			if (phc.update(oldKey, newKey) != null) {
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
