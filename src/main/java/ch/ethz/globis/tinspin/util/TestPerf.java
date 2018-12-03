/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTree.PhQuery;
import ch.ethz.globis.phtree.util.StringBuilderLn;

public class TestPerf {

	protected static final Random RND = new Random();
	protected static final int DEPTH = 24;
	protected static final int DIM = 6;
	protected static final long DEPTH_MASK = (1L<<DEPTH)-1L;
	
	protected static class MyObject {
		public int a0;
		int a1;
		int a2;
		int a3;
		int a4;
		int a5;
	}
	
	//HashSet
	//TreeSet  (ordered)
	//TreeMap
	
	
	// *********************************
	// ***   Generic part  
	// *********************************
	
	public static void main(String[] args) {
		new TestPerf().testPerf();
	}
	
	public void testPerf() {
		Object O = new Object();
		int N_DATA = 10000;
		int N_QUERY = 10000;
		
		//Create Java data
		for (int i = 0; i < N_DATA; i++) {
			MyObject o = createInstance(i);
			assertFalse(addInstance(i, o));
		}
		
		//create ND data
		PhTree<Object> ind = PhTree.create(DIM);
		long[] val = new long[DIM];
		for (int i = 0; i < N_DATA; i++) {
			for (int d = 0; d < DIM; d++) {
				val[d] = createIntValue(i, d);
			}
			assertNull(ind.put(val, O));
			//System.out.println("i=" + PhTree.toBinary(val, 32));
		}
		
		//query java
		long t1a = System.currentTimeMillis();
		for (int i = 0; i < N_QUERY; i++) {
			Iterator<MyObject> res = queryFirst(i);
			long v0 = i;
			assertEquals(v0, res.next().a0);
			assertFalse(res.hasNext());
		}
		long t2a = System.currentTimeMillis();
		
		//query ND
		long[] MIN = new long[DIM];
		long[] MAX = new long[DIM];
		Arrays.fill(MIN, Long.MIN_VALUE);
		Arrays.fill(MAX, Long.MAX_VALUE);
		long t1nd = System.currentTimeMillis();
		for (int i = 0; i < N_QUERY; i++) {
			//long v0 = i;
			//get single by value
			int nnn = createIntValue(-1, 3);
			MIN[3] = nnn;
			MAX[3] = nnn;
			//Iterator<long[]> res = ind.querySingle(3, nnn, nnn);
			@SuppressWarnings("unused")
			PhQuery<Object> res = ind.query(MIN, MAX);
			//get single value by id
			//Iterator<long[]> res = ind.querySingle(0, v0, v0);
			//get extent
			//Iterator<long[]> res = ind.querySingle(0, 0, N);
//			long[] v = res.next();
			//System.out.println("v1=" + PhTree.toBinary(v, 32));
			//assertEquals(v0, v[0]);
//			assertFalse(res.hasNext());
		}
		long t2nd = System.currentTimeMillis();

		System.out.println("Normal: " + (t2a-t1a) + "  " + (t2a-t1a)/(double)N_QUERY);
		System.out.println("ND-Ind: " + (t2nd-t1nd) + "  " + (t2nd-t1nd)/(double)N_QUERY);
		
		printStats();
	}
	public static int STAT_X0 = 0; //nodes visited (range-query)
	public static int STAT_X1 = 0; //iterator steps per node (range-query)
	public static int STAT_X2 = 0; //sub-nodes checked
	public static int STAT_X2f1 = 0;  //sub-node exists, but contained no results
	public static int STAT_X2f2 = 0;  //sub-node exists, but infix-check failed
	public static int STAT_X3 = 0;
	public static int STAT_X3f = 0;
	public static int STAT_X4 = 0;
	public static int STAT_X4sa = 0;
	public static int STAT_X4pa = 0;
	public static int STAT_X4pb = 0;
	public static int STAT_X4sb = 0;
	public static int STAT_X4pc = 0;
	public static int STAT_X4sc = 0;
	public static int STAT_X5 = 0;
	public static int STAT_X5a = 0;
	public static int STAT_X5ab = 0;
	public static int STAT_X5b = 0;
	public static int STAT_X6ph = 0;
	public static int STAT_X6pl = 0;
	public static int STAT_X6sh = 0;
	public static int STAT_X6sl = 0;
	public static int[] STAT_Alow = new int[64];
	public static int[] STAT_Aupp = new int[64];
	public static int STAT_Xlow = 0;
	public static int STAT_Xupp = 0;

	public static void printStats() {
		System.out.println(toStringOut());
	}
	
	public static String toStringOut() {
		StringBuilderLn s = new StringBuilderLn();
		s.appendLn("x0 = " + STAT_X0 + 
				"  x1 = " + STAT_X1 + 
				"  x2 = " + STAT_X2 + "/" + STAT_X2f1 + "/" + STAT_X2f2 +
				"  x3 = " + STAT_X3 + "/" + STAT_X3f);
		s.appendLn("x4 = " + STAT_X4 + 
				"  x4p = " + STAT_X4pa + "/" + STAT_X4pb + "/" + STAT_X4pc +
				"  x4s = " + STAT_X4sa + "/" + STAT_X4sb + "/" + STAT_X4sc);
		s.appendLn("x5/a/ab/b = " + STAT_X5 + "/" + STAT_X5a + "/" + STAT_X5ab + "/" + STAT_X5b);
		s.appendLn("x6p = " + STAT_X6ph + " / " + STAT_X6pl +   
				"  x6s = " + STAT_X6sh + "/" + STAT_X6sl);
		s.appendLn("xAlow = " + STAT_Xlow + ": " + Arrays.toString(STAT_Alow));
		s.appendLn("xAupp = " + STAT_Xupp + ": " + Arrays.toString(STAT_Aupp));
		return s.toString();
	}
	
	public static void resetStats() {
		TestPerf.STAT_X0 = 0;
		TestPerf.STAT_X1 = 0;
		TestPerf.STAT_X2 = 0;
		TestPerf.STAT_X2f1 = 0;
		TestPerf.STAT_X2f2 = 0;
		TestPerf.STAT_X3 = 0;
		TestPerf.STAT_X3f = 0;
		TestPerf.STAT_X4 = 0;
		TestPerf.STAT_X4sa = 0;
		TestPerf.STAT_X4pa = 0;
		TestPerf.STAT_X4pb = 0;
		TestPerf.STAT_X4sb = 0;
		TestPerf.STAT_X4pc = 0;
		TestPerf.STAT_X4sc = 0;
		TestPerf.STAT_X5 = 0;
		TestPerf.STAT_X5a = 0;
		TestPerf.STAT_X5ab = 0;
		TestPerf.STAT_X5b = 0;
		TestPerf.STAT_X6ph = 0;
		TestPerf.STAT_X6pl = 0;
		TestPerf.STAT_X6sh = 0;
		TestPerf.STAT_X6sl = 0;
		Arrays.fill(STAT_Alow, 0);
		Arrays.fill(STAT_Aupp, 0);
		TestPerf.STAT_Xlow = 0;
		TestPerf.STAT_Xupp = 0;
	}
	
	/**
	 * 
	 * @param id ID
	 * @param attrID attribute ID
	 * @return The value for a given attribute for a given object. 
	 */
	protected int createIntValue(long id, int attrID) {
		//return (int) (DEPTH_MASK & (attrID == 0 ? (int)id : RND.nextInt()>>>24));
		return (int) (DEPTH_MASK & (attrID == 0 ? (int)id : RND.nextInt()>>>16));
	}
	
	protected MyObject createInstance(long id) {
		MyObject o = new MyObject();
		o.a0 = createIntValue(id, 0);
		o.a1 = createIntValue(id, 1);
		o.a2 = createIntValue(id, 2);
		o.a3 = createIntValue(id, 3);
		o.a4 = createIntValue(id, 4);
		o.a5 = createIntValue(id, 5);
		return o;
	}
	
	// *********************************
	// ***   Specific part  
	// *********************************
	
	private final HashMap<Long, MyObject> map = new HashMap<Long, MyObject>();
	
	protected boolean addInstance(long id, MyObject o) {
		return map.put(id, o) != null;
	}
	
	protected Iterator<MyObject> queryFirst(int v) {
		MyObject o = map.get((long)v);
		//To make result comparable, we enforce returning an Iterator, because  
		//we should not expect there to be only a single match. 
		LinkedList<MyObject> l = new LinkedList<MyObject>();
		l.add(o);
		return l.iterator();
	}
	
}
