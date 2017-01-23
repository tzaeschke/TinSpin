/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import ch.ethz.globis.phtree.PhTreeHelper;
import ch.ethz.globis.tinspin.TestStats.IDX;
import ch.ethz.globis.tinspin.TestStats.TST;
import ch.ethz.globis.tinspin.data.AbstractTest;
import ch.ethz.globis.tinspin.data.TestPoint;
import ch.ethz.globis.tinspin.data.TestRectangle;
import ch.ethz.globis.tinspin.util.JmxTools;
import ch.ethz.globis.tinspin.util.MemTools;
import ch.ethz.globis.tinspin.util.TestPerf;
import ch.ethz.globis.tinspin.wrappers.Candidate;
import ch.ethz.globis.tinspin.wrappers.PointPHCCTree;


/**
 * Main test runner class.
 * The test runner can be executed directly or remotely in a separate
 * process via the TestManager.
 *
 * @author Tilmann Zaeschke
 */
public class TestRunner {
	
	private static final SimpleDateFormat FT = new SimpleDateFormat ("yyyy-MM-dd' 'HH:mm:ss");
	private static final boolean DEBUG = PhTreeHelper.DEBUG;
	
	public static boolean USE_NEW_QUERIES = true;

	private final TestStats S;
	private Random R;
	private double[] data;
	private Candidate tree;
	private AbstractTest test = null;

	
	public static void main(String[] args) {
		//-Xmx28G -XX:+UseConcMarkSweepGC -Xprof -XX:MaxInlineSize=0 -XX:FreqInlineSize=0 -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining 
		//-XX:+PrintHeapAtGC - Prints detailed GC info including heap occupancy before and after GC
		//-XX:+PrintTenuringDistribution - Prints object aging or tenuring information
		if (args.length > 0) {
			runWithArgs(args);
			return;
		}
		
		final int DIM = 3;
		final int N = 1*1000*1000;
						
		//TestStats s0 = new TestStats(TST.CLUSTER, IDX.QKDZ, N, DIM, true, 5);
		//TestStats s0 = new TestStats(TST.CUBE, IDX.QKDZ, N, DIM, true, 1.0);
		//TestStats s0 = new TestStats(TST.OSM, IDX.PHC, N, 2, true, 1.0);
		//TestStats s0 = new TestStats(TST.CUBE, IDX.PHC, N, DIM, true, 1.0E-5);
		//TestStats s0 = new TestStats(TST.CLUSTER, IDX.RSZ, N, DIM, false, 3.4);
		TestStats s0 = new TestStats(TST.CUBE, IDX.RSZ, N, DIM, false, 1.0);
		//TestStats s0 = new TestStats(TST.OSM, IDX.PHC, N, 2, false, 1.0);
		//s0.cfgWindowQueryRepeat = 1000;
		//s0.cfgPointQueryRepeat = 1000000;
		//s0.cfgUpdateSize = 1000;

//		s0.cfgWindowQuerySize = 1;

		s0.setSeed(0);
		TestRunner test = new TestRunner(s0);
		TestStats s = test.run();
		System.out.println(s);
		//System.out.println(BitsLong.POOL.print());
//		System.out.println("AMM: " + PhIteratorNoGC.AMM1 + " / " + PhIteratorNoGC.AMM2 + " / " + PhIteratorNoGC.AMM3 + " / " + PhIteratorNoGC.AMM4 + " / " + PhIteratorNoGC.AMM5 + " / ");
//		System.out.println("VMM: " + PhIteratorNoGC.VMM1 + " / " + PhIteratorNoGC.VMM2 + " / " + PhIteratorNoGC.VMM3);
//		System.out.println("HCI-A/L/N: " + PhIteratorNoGC.HCIA + " / " + PhIteratorNoGC.HCIL + " / " + PhIteratorNoGC.HCIN);
	}
	
	private static void runWithArgs(String[] args) {
		if (args.length < 4) {
			System.out.println("ERROR: At least 4 arguments required, found: " + args.length);
			System.out.println("Example: TestRunner CUBE PHC 1000000 3 <true/false> "
					+ "<1.0/3.4/3.5/...> <0/1/2/...>");
			System.out.println("Example: TestRunner [TEST] [INDEX] [SIZE] [DIM] "
					+ "[BOXES=false] [param=1.0] [random seed=0]");
			return;
		}
		TST tst;
		try {
			tst = TST.valueOf(args[0]);
		} catch (IllegalArgumentException e) {
			System.out.println("Test not recognised: " + args[0]);
			System.out.print("Please choose one of: ");
			for (TST t: TST.values()) {
				System.out.print(t.name() + ", ");
			}
			return;
		}
		
		IDX idx;
		try {
			idx = IDX.valueOf(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("Index not recognised: " + args[0]);
			System.out.print("Please choose one of: ");
			for (IDX t: IDX.values()) {
				System.out.print(t.name() + ", ");
			}
			return;
		}
		
		int n = Integer.parseInt(args[2]);
		int dim = Integer.parseInt(args[3]);
		boolean box = args.length > 4 ? Boolean.parseBoolean(args[4]) : false;
		double param0 = args.length > 5 ? Double.parseDouble(args[5]) : 1.0;
		int seed = args.length > 6 ? Integer.parseInt(args[6]) : 0;

		TestStats s0 = new TestStats(tst, idx, n, dim, box, param0);
		s0.setSeed(seed);
		TestRunner test = new TestRunner(s0);
		TestStats s = test.run();
		System.out.println(s);
		return;
	}

	public TestRunner(TestStats S) { 
		this.S = S;
		this.R = new Random(S.seed);
	}
	
	public TestStats run() {
		JmxTools.startUp();

		//load
		resetR();
		load(S);
		
//		if (false) {
//			TestDraw.draw(data, 2);
//			return S;
//		}
		
		//window queries
		if (tree.supportsWindowQuery()) {
			resetR();
			repeatQuery(S.cfgWindowQueryRepeat, 0);
			repeatQuery(S.cfgWindowQueryRepeat, 1);
			S.assortedInfo += " WINDOW_RESULTS=" + S.cfgWindowQuerySize;
		} else {
			System.err.println("WARNING: window queries disabled");
		}
		
		//point queries.
		if (tree.supportsPointQuery()) {
			resetR();
			repeatPointQuery(S.cfgPointQueryRepeat, 0);
			repeatPointQuery(S.cfgPointQueryRepeat, 1);
		} else {
			System.err.println("WARNING: point queries disabled");
		}

		//kNN queries
		if (tree.supportsKNN()) {
			int repeat = getKnnRepeat(S.cfgNDims);
			S.assortedInfo += " KNN_REPEAT=" + repeat;
			resetR(12345);
			repeatKnnQuery(repeat, 0, 1);
			repeatKnnQuery(repeat, 1, 1);
			repeatKnnQuery(repeat, 0, 10);
			repeatKnnQuery(repeat, 1, 10);
		} else {
			System.err.println("WARNING: kNN queries disabled");
		}
		
		//update
		if (tree.supportsUpdate()) {
			S.assortedInfo += " UPD_DIST=" + test.maxUpdateDistance();
			resetR();
			update(0);
			update(1);
		} else {
			System.err.println("WARNING: update() disabled");
		}
		
		//unload
		if (tree.supportsUnload()) {
			unload();
		} else {
			System.err.println("WARNING: unload() disabled");
		}

		if (tree != null) {
			tree.release();
		}
		
		return S;
	} 
	
	/**
	 * This method sets the random seed to the default seed plus a given delta.
	 * This solves the problem that, for example, the kNN generator
	 * would generate the same points as the data generator, which
	 * resulted in 0.0 distance for all queried points. 
	 * @param delta
	 */
	private void resetR(int delta) {
		R.setSeed(S.seed + delta);
	}

	private void resetR() {
		R.setSeed(S.seed);
	}
	
	private int getKnnRepeat(int dims) {
		if (S.TEST == TestStats.TST.CLUSTER && S.cfgNDims > 5 ) {
			S.cfgKnnQueryBaseRepeat /= 10;//100;
		}
		if (dims <= 3) {
			return S.cfgKnnQueryBaseRepeat;
		}
		if (dims <= 6) {
			return S.cfgKnnQueryBaseRepeat/10;
		}
		if (dims <= 10) {
			return S.cfgKnnQueryBaseRepeat/10;
		}
		return S.cfgKnnQueryBaseRepeat/50;
	}
	
	private void load(TestStats ts) {
		log(time() + "generating data ...");
		long t1g = System.currentTimeMillis();

		if (ts.isRangeData) {
			test = TestRectangle.create(R, ts);
		} else {
			test = TestPoint.create(R, ts);
		}
		
		switch (ts.TEST) {
		case CUBE:
		case CLUSTER:
		case CSV:
		case OSM:
		case TIGER:
		case TOUCH:
		case VORTEX: {
			data = test.generate();
			break;
		}
		//case ASPECT:
		case MBR_SIZE: {
			//IS_POINT_DATA = PR_TestSize.generate(R, cfgDataLen, N, DIM, 0.001f);
			//IS_POINT_DATA = PR_TestSize.generate(R, cfgDataLen, N, DIM, 0.02f);
			//data = PR_TestAspect.generate(R, cfgDataLen, N, DIM, 1e3f);//10.0f);
			data = test.generate();
			if (!ts.isRangeData) throw new IllegalStateException();
			break;
		}
		case CUSTOM: {
			if (S.testClass == null)
				throw new RuntimeException("No dataset class passed (null)");
			
			try {
				// Note: a custom Test class MUST have an empty constructor
				test = S.testClass.getDeclaredConstructor().newInstance();
				data = test.generate();
				break;
			} catch (Exception e) {
				throw new RuntimeException("Failed to generate custom dataset.", e);
			}
		}
		default:
			throw new UnsupportedOperationException("No data for: " + ts.TEST.name());
		}
		long t2g = System.currentTimeMillis();
		log("data generation finished in: " + (t2g-t1g));
		S.statTGen = t2g-t1g;
		
		int dims = S.cfgNDims;
		int N = S.cfgNEntries;
		
        long memTree = MemTools.getMemUsed();
		if (ts.paramEnforceGC) {
			MemTools.cleanMem(N, memTree);
		}

		
		//load index
		log(time() + "loading index ...");
        memTree = MemTools.getMemUsed();
        JmxTools.reset();
		long t1 = System.currentTimeMillis();
		
		tree = ts.createTree();
		tree.load(data, dims);

		long t2 = System.currentTimeMillis();
		S.statGcDiffL = JmxTools.getDiff();
		S.statGcTimeL = JmxTools.getTime();
		log("loading finished in: " + (t2-t1));
		if (ts.paramEnforceGC) {
			S.statSjvmF = MemTools.cleanMem(N, memTree);
		}
		S.statSjvmE = S.statSjvmF / N;
		S.statTLoad = t2-t1;
		
		if (ts.INDEX == IDX.PHCC) {
			// TODO: add pht-cpp statistics collection
			
			// memory usage
			S.statSjvmF = ((PointPHCCTree)tree).getMemoryUsage();
			S.statSjvmE = S.statSjvmF / N;
		}
		
		tree.getStats(S);
		S.assortedInfo += tree.toString();
		
		//This avoid premature garbage collection...
		log("loaded objects: " + N + " " + data[0]);
	}
		
	private void repeatQuery(int repeat, int round) {
		int dims = S.cfgNDims;
		log("N=" + S.cfgNEntries);
		log(time() + "querying index ... repeat = " + repeat);
		double[][] lower = new double[repeat][dims]; 
		double[][] upper = new double[repeat][dims];
		test.generateWindowQueries(lower, upper);
		JmxTools.reset();
		long t1 = System.currentTimeMillis();
		int n = 0;
		if (tree.supportsWindowQuery()) {
			n = repeatQueries(lower, upper);
		} else {
			n = -1;
		}
		long t2 = System.currentTimeMillis();
		log("Query time: " + (t2-t1) + " ms -> " + (t2-t1)/(double)repeat + " ms/q -> " +
				(t2-t1)*1000*1000/(double)n + " ns/q/r  (n=" + n + ")");
		if (round == 0) {
			S.statTq1 = (t2-t1);
			S.statTq1E = (long) ((t2-t1)*1000*1000/(double)n);
			S.statNq1 = n;
		} else {
			S.statTq2 = (t2-t1);
			S.statTq2E = (long) ((t2-t1)*1000*1000/(double)n);
			S.statNq2 = n;
		}
		S.statGcDiffWq = JmxTools.getDiff();
		S.statGcTimeWq = JmxTools.getTime();
	}
	
	private void repeatPointQuery(int repeat, int round) {
		log(time() + "point queries ...");
		//prepare query
		//TODO return only double[], convert inside query function!
		double[][] qDA = preparePointQuery(repeat);
		Object q = tree.preparePointQuery(qDA);
		JmxTools.reset();
		
		//query
		long t1 = System.currentTimeMillis();
		int n = tree.pointQuery(q);
		long t2 = System.currentTimeMillis();
		log("Elements found: " + n + " -> " + n/(double)repeat);
		log("Query time: " + (t2-t1) + " ms -> " + (t2-t1)/(double)repeat + " ms/q -> " +
				(t2-t1)*1000*1000/(double)repeat + " ns/q");
		if (round == 0) {
			S.statTqp1 = (t2-t1);
			S.statTqp1E = (long) ((t2-t1)*1000*1000/(double)repeat);
			S.statNqp1 = n;
		} else {
			S.statTqp2 = (t2-t1);
			S.statTqp2E = (long) ((t2-t1)*1000*1000/(double)repeat);
			S.statNqp2 = n;
		}
		S.statGcDiffPq = JmxTools.getDiff();
		S.statGcTimePq = JmxTools.getTime();
	}
	
	private double[][] preparePointQuery(int repeat) {
		int dims = S.cfgNDims;
		double[][] qA;
		if (!S.isRangeData) {
			qA = new double[repeat][];
			for (int i = 0; i < repeat; i++) {
				qA[i] = generateQueryPointD(S.cfgNEntries, dims);
			}
		} else {
			qA = new double[repeat*2][];
			for (int i = 0; i < repeat; i++) {
				double[] lo = new double[dims];
				double[] hi = new double[dims];
				generateQueryPointDRect(lo, hi, S.cfgNEntries, dims);
				qA[2*i] = lo;
				qA[2*i+1] = hi;
			}
		}
		return qA;
	}

	private void repeatKnnQuery(int repeat, int round, int k) {
		log(time() + "kNN queries ...");
		//prepare query
		double[][] q = prepareKnnQuery(repeat);
		JmxTools.reset();
		
		//query
		double dist = 0;
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < repeat; i++) {
			dist += tree.knnQuery(k, q[i]);
		}
		long t2 = System.currentTimeMillis();
		double avgDist = dist/repeat/k;
		log("Element distance: " + dist + " -> " + avgDist);
		log("kNN query time: " + (t2-t1) + " ms -> " + (t2-t1)/(double)repeat + " ms/q -> " +
				(t2-t1)*1000*1000/(double)k + " ns/q/r");
		if (k == 1) {
			if (round == 0) {
				S.statTqk1_1 = t2-t1;
				S.statTqk1_1E = (long) ((t2-t1)*1000*1000/(double)repeat);
				S.statDqk1_1 = avgDist;
			} else {
				S.statTqk1_2 = t2-t1;
				S.statTqk1_2E = (long) ((t2-t1)*1000*1000/(double)repeat);
				S.statDqk1_2 = avgDist;
			}
			S.statGcDiffK1 = JmxTools.getDiff();
			S.statGcTimeK1 = JmxTools.getTime();
		} else {
			if (round == 0) {
				S.statTqk10_1 = t2-t1;
				S.statTqk10_1E = (long) ((t2-t1)*1000*1000/(double)repeat);
				S.statDqk10_1 = avgDist;
			} else {
				S.statTqk10_2 = t2-t1;
				S.statTqk10_2E = (long) ((t2-t1)*1000*1000/(double)repeat);
				S.statDqk10_2 = avgDist;
			}
			S.statGcDiffK10 = JmxTools.getDiff();
			S.statGcTimeK10 = JmxTools.getTime();
		}
	}
	
	private double[][] prepareKnnQuery(int repeat) {
		int dims = S.cfgNDims;
		double[][] qA;
		if (!S.isRangeData) {
			qA = new double[repeat][];
			for (int i = 0; i < repeat; i++) {
				qA[i] = generateKnnQueryPointD(dims);
			}
		} else {
			qA = new double[repeat*2][];
			for (int i = 0; i < repeat; i++) {
				double[] lo = new double[dims];
				double[] hi = new double[dims];
				generateKnnQueryPointDRect(lo, hi, dims);
				qA[2*i] = lo;
				qA[2*i+1] = hi;
			}
		}
		return qA;
	}

	@SuppressWarnings("unused")
	private static class Hist implements Comparable<Hist> {
		long t1, t2, X1, X2, X2f, X3, X3f;
		long X4, X4pa, X4pb, X4pc, X4sa, X4sb, X4sc;
		long X0, X2f1, X2f2, X5, X5a, X5b, X5ab;
		public Hist() {
			TestPerf.resetStats();
			t1 = System.currentTimeMillis();
		}
		void close() {
			t2 = System.currentTimeMillis();
			X0 += TestPerf.STAT_X0;
			X1 += TestPerf.STAT_X1;
			X2 += TestPerf.STAT_X2;
			X2f1 += TestPerf.STAT_X2f1;
			X2f2 += TestPerf.STAT_X2f2;
			X3 += TestPerf.STAT_X3;
			X3f += TestPerf.STAT_X3f;
			X4 += TestPerf.STAT_X4;
			X4pa += TestPerf.STAT_X4pa;
			X4pb += TestPerf.STAT_X4pb;
			X4pc += TestPerf.STAT_X4pc;
			X4sa += TestPerf.STAT_X4sa;
			X4sb += TestPerf.STAT_X4sb;
			X4sc += TestPerf.STAT_X4sc;
			X5 += TestPerf.STAT_X5;
			X5a += TestPerf.STAT_X5a;
			X5b += TestPerf.STAT_X5b;
			X5ab += TestPerf.STAT_X5ab;
		}
		@Override
		public int compareTo(Hist o) {
			return (int) ((t2-t1)-(o.t2-o.t1));
		}
		@Override
		public String toString() {
			return "dT=" + (t2-t1) + " X1=" + X1 + 
					" X2=" + X2 + " X2f=" + X2f + " X2f1/f2=" + X2f1 + "/" + X2f2 + 
					" X3=" + X3 + " X3f=" + X3f +
					" X5=" + X5 + " X5a/b=" + X5a + "/" + X5b;
		}
		/**
		 * @param histList histograms
		 * @return summary in String form
		 */
		public static String summary(ArrayList<Hist> histList) {
			Hist sum = new Hist();
			for (Hist h: histList) {
				sum.X0 += h.X0;
				sum.X1 += h.X1;
				sum.X2 += h.X2;
				sum.X2f1 += h.X2f1;
				sum.X2f2 += h.X2f2;
				sum.X3 += h.X3;
				sum.X3f += h.X3f;
				sum.X5 += h.X5;
				sum.X5a += h.X5a;
				sum.X5b += h.X5b;
			}
			return sum.toString();
		}
	}
	
	private int repeatQueries(double[][] lower, double[][] upper) {
		int n=0;
		for (int i = 0; i < lower.length; i++) {
			n += tree.query(lower[i], upper[i]);
			if (i%10 == 0) System.out.print('.');
		}
		System.out.println();
		TestRunner.log("n=" + n/(double)lower.length);
		if (DEBUG) {
			log(TestPerf.toStringOut());
			TestPerf.resetStats();
		}
		return n;
	}
	
	static void log(String string) {
		System.out.println(string);
	}


	/**
	 * Generates a random cuboid with fixed size=0.1^DIM, for example 0.001 for DIM=2.
	 * @param xyz lower left corner
	 * @param len lengths of edges
	 */
//	private void generateQueryCorners(double[] min, double[] max) {
//		if (test.getTestType() == TST.CLUSTER) {
//			test.queryCuboid(S.cfgWindowQuerySize, min, max);
//			return;
//		} else if (test.getTestType() == TST.TIGER) {
//			if (test.getTestStats().isRangeData) {
//				test.queryCuboid(S.cfgWindowQuerySize, min, max);
//			}
//			return;
//		} else if (test.getTestType() == TST.CUSTOM) {
//			test.queryCuboid(S.cfgWindowQuerySize, min, max);
//			return;
//		}
//
//		if (USE_NEW_QUERIES) {
//			generateQueryCornersNew(min, max);
//		} else {
//			generateQueryCornersOld(min, max);
//		}
//	}
	

//	private void generateQueryCornersOld(double[] min, double[] max) {
//		int dims = min.length;
//		//Here is a fixed size version, returning 1% of the space.
//		//final double qVolume = 0.01 * Math.pow(cfgDataLen, DIM);//(float) Math.pow(0.1, DIM); //0.01 for DIM=2
//		final double qVolume = S.cfgWindowQuerySize/(double)S.cfgNEntries * Math.pow(S.cfgDataLen, dims);
//		
//		int dDrop = R.nextInt(dims);
//		//query create cube
//		double[] len = new double[min.length];
//		do {
//			double vol = 1;
//			for (int d = 0; d < dims; d++) {
//				if (d == dDrop) {
//					continue;
//				}
//				len[d] = R.nextDouble()*S.cfgDataLen;
//				vol *= len[d];
//			}
//			//create cuboid/box of desired size by dropping random length
//			len[dDrop] = qVolume/vol;  //now the new len creates a rectangle/box of SIZE.
//		} while (len[dDrop] >= S.cfgDataLen); //drop bad rectangles
//		
//		//create location
//		for (int d = 0; d < dims; d++) {
//			min[d] = R.nextDouble()*(S.cfgDataLen-len[d]);
//			max[d] = min[d]+len[d];
//			if (min[d]+len[d] >= S.cfgDataLen) {
//				//drop bad rectangles 
//				throw new RuntimeException();
//			}
//		}
//	}

	
	private double[] generateQueryPointD(final int N, final int dims) {
		double[] xyz = new double[dims];
		int pos = R.nextInt(N*2);
		if (pos >= N) {
			//randomise
			for (int d = 0; d < dims; d++) {
				xyz[d] = test.min(d) + R.nextDouble()*test.len(d);
			}
		} else {
			//get existing element
			System.arraycopy(data, pos*dims, xyz, 0, dims);
		}
		return xyz;
	}
	
	private void generateQueryPointDRect(double[] lo, double[] hi, final int N, final int dims) {
		int pos = R.nextInt(N*2);
		if (pos >= N) {
			//randomise
			for (int d = 0; d < dims; d++) {
				lo[d] = test.min(d) + R.nextDouble()*test.len(d);
				hi[d] = lo[d] + R.nextDouble()*test.len(d)/1000.;
			}
		} else {
			//get existing element
			System.arraycopy(data, pos*dims*2, lo, 0, dims);
			System.arraycopy(data, pos*dims*2+dims, hi, 0, dims);
		}
	}
	
	private double[] generateKnnQueryPointD(final int dims) {
		double[] xyz = new double[dims];
		//randomise
		for (int d = 0; d < dims; d++) {
			xyz[d] = test.min(d) + R.nextDouble()*test.len(d);
		}
		return xyz;
	}
	
	private void generateKnnQueryPointDRect(double[] lo, double[] hi, final int dims) {
		//randomise
		for (int d = 0; d < dims; d++) {
			lo[d] = test.min(d) + R.nextDouble()*test.len(d);
			hi[d] = lo[d] + R.nextDouble()*test.len(d)/1000.;
		}
	}
	
	private void update(int round) {
		log(time() + "updates ...");
		
		int n = 0;
		long t = 0;
		double[][] u = null; //2 points, 2 versions
		int nUpdates = S.cfgUpdateSize > S.cfgNEntries/4 ? S.cfgNEntries/4 : S.cfgUpdateSize;
		for (int i = 0; i < S.cfgUpdateRepeat; i++) {
			//prepare query
			u = test.generateUpdates(nUpdates, data, u);
			JmxTools.reset();
			//updates
			long t1 = System.currentTimeMillis();
			n += tree.update(u);
			long t2 = System.currentTimeMillis();
			t += t2-t1;
			S.statGcDiffUp += JmxTools.getDiff();
			S.statGcTimeUp += JmxTools.getTime();
		}
		
		log("Elements updated: " + n + " -> " + n);
		log("Update time: " + t + " ms -> " + t*1000*1000/(double)n + " ns/update");
		if (round == 0) {
			S.statTu1 = t;
			S.statTu1E = (long) (t*1000*1000/(double)n);
			S.statNu1 = n;
		} else {
			S.statTu2 = t;
			S.statTu2E = (long) (t*1000*1000/(double)n);
			S.statNu2 = n;
		}
	}
	
	private void unload() {
		log("Unloading...");
		JmxTools.reset();

		long t1 = System.currentTimeMillis();
		int n = tree.unload();
		long t2 = System.currentTimeMillis();
		
		log("Deletion time: " + (t2-t1) + " ms -> " + 
		(t2-t1)*1000*1000/(double)S.cfgNEntries + " ns/q/r");
		S.statTUnload = t2-t1;
		S.statGcDiffUl = JmxTools.getDiff();
		S.statGcTimeUl = JmxTools.getTime();
		if (S.cfgNEntries != n) {
			System.err.println("Delete N/n: " + S.cfgNEntries + "/" + n);
			//throw new IllegalStateException("N/n: " + S.cfgNEntries + "/" + n);
		}
	}
	
	public TestStats getTestStats() {
		return S;
	}
	
	private String time() {
		return FT.format(new Date()) + " ";
	}

	public Candidate getCandidate() {
		return tree;
	}
}
