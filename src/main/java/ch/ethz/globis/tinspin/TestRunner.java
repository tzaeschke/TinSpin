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
	public static long minimumMS = 2000; 

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
		final int N = 10_000;

		//TestStats s0 = new TestStats(TST.CLUSTER, IDX.QTZ, N, DIM, true, 5);
		//TestStats s0 = new TestStats(TST.CUBE, IDX.QTZ, N, DIM, true, 1.0);
		//TestStats s0 = new TestStats(TST.OSM, IDX.PHC, N, 2, true, 1.0);
		//TestStats s0 = new TestStats(TST.CUBE, IDX.PHC, N, DIM, true, 1.0E-5);
		//TestStats s0 = new TestStats(TST.CLUSTER, IDX.FCT, N, DIM, false, 5.0);
		//TestStats s0 = new TestStats(TST.CUBE, IDX.PHC, N, DIM, false, 1.0);
		//TestStats s0 = new TestStats(TST.OSM, IDX.PHC2, N, 2, false, 1.0);
		TestStats s0 = new TestStats(TST.HDF5, IDX.KDZ, N, DIM, false, 4.0);
		//s0.cfgWindowQueryRepeat = 1000;
		//s0.cfgPointQueryRepeat = 1000000;
		//s0.cfgUpdateSize = 1000;

		//s0.cfgWindowQuerySize = 1;
		//s0.cfgWindowQueryRepeat = 10_000;

		//s0.cfgWindowQuerySize = 1;
		//s0.cfgKnnQueryBaseRepeat = 1000_000;

		//s0.cfgWindowQuerySize = 10000;
		//s0.cfgWindowQueryRepeat = 10000;

		s0.setSeed(0);
		TestRunner test = new TestRunner(s0);
		TestStats s = test.run();
		System.out.println(test.tree.getNativeStats());
		System.out.println(s.toStringOld());
		System.out.println(s.toStringNew());
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

		//		if (!false) {
		//			TestDraw.draw(data, 2);
		//			return S;
		//		}

		//		int resolution = 10000;
		//		for (int d = 0; d < S.cfgNDims; d++) {
		//			int[] histo = new int[resolution];
		//			for (int i = d; i < S.cfgNEntries; i+=S.cfgNDims) {
		//				double dat = data[i]; 
		//				histo[(int)(dat*resolution)]++;
		//			}
		//			System.out.println("histo(d=" + d + "): " + Arrays.toString(histo));
		//		}


		//window queries
		if (S.cfgNDims <= 60 && tree.supportsWindowQuery()) {
			resetR();
			repeatQuery(S.cfgWindowQueryRepeat, 0);
			repeatQuery(S.cfgWindowQueryRepeat, 1);
			S.assortedInfo += " WINDOW_RESULTS=" + S.cfgWindowQuerySize;
		} else if (S.cfgNDims > 60 ) {
			System.err.println("WARNING: skipping window queries for dims=" + S.cfgNDims);
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

		tree.getStats(S);

		tree.release();

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
		log(date() + "generating data ...");
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
		case HDF5:
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
		log(date() + "loading index ...");
		memTree = MemTools.getMemUsed();
		JmxTools.reset();
		long t1 = timer();

		tree = ts.createTree();
		tree.load(data, dims);

		long t2 = timer();
		S.statGcDiffL = JmxTools.getDiff();
		S.statGcTimeL = JmxTools.getTime();
		log("loading finished in: " + (long)toMS(t1, t2) + "ms");
		if (ts.paramEnforceGC) {
			S.statSjvmF = MemTools.cleanMem(N, memTree);
		}
		S.statSjvmE = S.statSjvmF / N;
		S.statTLoad = (long) toMS(t1, t2);
		S.statPSLoad = opsPerSec(N, t1, t2);

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
		log(date() + "querying index ... repeat = " + repeat);
		double[][] lower = new double[repeat][dims]; 
		double[][] upper = new double[repeat][dims];
		test.generateWindowQueries(lower, upper);

		long t00 = timer();
		int n;
		long t1, t2;
		//Use result count from first run as control value
		int control = -1;
		do {
			JmxTools.reset();
			t1 = timer();
			n = 0;
			if (tree.supportsWindowQuery()) {
				n = repeatQueries(lower, upper);
			} else {
				n = -1;
			}
			t2 = timer();
			if (control == -1) {
				control = n;
			}
			logNLF("*");
		} while (toMS(t00, timer()) < minimumMS);
		if (t2 == t1) {
			t2++;
		}

		log("n/q=" + n/(double)lower.length);
		log("Query time: " + toMS(t1, t2) + " ms -> " + 
				toMS(t1, t2)/(double)repeat + " ms/q -> " +
				toNSPerOp(t1, t2, n) + " ns/q/r  (n=" + n + ")");
		if (round == 0) {
			S.statTq1 = (long) toMS(t1, t2);
			S.statTq1E = toNSPerOp(t1, t2, repeat);
			S.statPSq1 = opsPerSec(repeat, t1, t2);
			S.statNq1 = control;
		} else {
			S.statTq2 = (long) toMS(t1, t2);
			S.statTq2E = toNSPerOp(t1, t2, repeat);
			S.statPSq2 = opsPerSec(repeat, t1, t2);
			S.statNq2 = control;
		}
		S.statGcDiffWq = JmxTools.getDiff();
		S.statGcTimeWq = JmxTools.getTime();
	}

	private void repeatPointQuery(int repeat, int round) {
		log(date() + "point queries ...");
		//prepare query
		//TODO return only double[], convert inside query function!
		double[][] qDA = preparePointQuery(repeat);
		Object q = tree.preparePointQuery(qDA);

		long t00 = timer();
		int n;
		long t1, t2;
		//Use result count from first run as control value
		int control = -1;
		do {
			JmxTools.reset();

			//query
			t1 = timer();
			n = tree.pointQuery(q);
			t2 = timer();
			if (control == -1) {
				control = n;
			}
			logNLF("*");
		} while (toMS(t00, timer()) < minimumMS);
		if (t2 == t1) {
			t2++;
		}

		log("Elements found: " + n + " -> " + n/(double)repeat);
		log("Query time: " + toMS(t1, t2) + " ms -> " + 
				toMS(t1, t2)/(double)repeat + " ms/q -> " +
				toNSPerOp(t1, t2, repeat) + " ns/q");
		if (round == 0) {
			S.statTqp1 = (long) toMS(t1, t2);
			S.statTqp1E = toNSPerOp(t1, t2, repeat);
			S.statPSqp1 = opsPerSec(repeat, t1, t2);
			S.statNqp1 = control;
		} else {
			S.statTqp2 = (long) toMS(t1, t2);
			S.statTqp2E = toNSPerOp(t1, t2, repeat);
			S.statPSqp2 = opsPerSec(repeat, t1, t2);
			S.statNqp2 = control;
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
		log(date() + "kNN queries ...");
		//prepare query
		double[][] q = prepareKnnQuery(repeat);

		long t00 = timer();
		long t1, t2;
		double dist;
		//Use average distance of first run as control value
		double control = -1;
		do {
			JmxTools.reset();

			//query
			dist = 0;
			t1 = timer();
			for (int i = 0; i < repeat; i++) {
				dist += tree.knnQuery(k, q[i]);
			}
			t2 = timer();
			if (control == -1) {
				control = dist/repeat/k;
			}
			logNLF("*");
		} while (toMS(t00, timer()) < minimumMS);
		if (t2 == t1) {
			t2++;
		}

		log("Element distance: " + dist + " -> " + control);
		log("kNN query time (repeat=" + repeat + "): " + toMS(t1, t2) + " ms -> " + 
				toMS(t1, t2)/(double)repeat + " ms/q -> " +
				toNSPerOp(t1, t2, k*repeat) + " ns/q/r");
		if (k == 1) {
			if (round == 0) {
				S.statTqk1_1 = (long) toMS(t1, t2);
				S.statTqk1_1E = toNSPerOp(t1, t2, repeat);
				S.statPSqk1_1 = opsPerSec(repeat, t1, t2);
				S.statDqk1_1 = control;
			} else {
				S.statTqk1_2 = (long) toMS(t1, t2);
				S.statTqk1_2E = toNSPerOp(t1, t2, repeat);
				S.statPSqk1_2 = opsPerSec(repeat, t1, t2);
				S.statDqk1_2 = control;
			}
			S.statGcDiffK1 = JmxTools.getDiff();
			S.statGcTimeK1 = JmxTools.getTime();
		} else {
			if (round == 0) {
				S.statTqk10_1 = (long) toMS(t1, t2);
				S.statTqk10_1E = toNSPerOp(t1, t2, repeat);
				S.statPSqk10_1 = opsPerSec(repeat, t1, t2);
				S.statDqk10_1 = control;
			} else {
				S.statTqk10_2 = (long) toMS(t1, t2);
				S.statTqk10_2E = toNSPerOp(t1, t2, repeat);
				S.statPSqk10_2 = opsPerSec(repeat, t1, t2);
				S.statDqk10_2 = control;
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
		long t1ns, t2ns, X1, X2, X2f, X3, X3f;
		long X4, X4pa, X4pb, X4pc, X4sa, X4sb, X4sc;
		long X0, X2f1, X2f2, X5, X5a, X5b, X5ab;
		public Hist() {
			TestPerf.resetStats();
			t1ns = timer();
		}
		void close() {
			t2ns = timer();
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
			return (int) ((t2ns-t1ns)-(o.t2ns-o.t1ns));
		}
		@Override
		public String toString() {
			return "dT=" + toMS(t1ns, t2ns) + " X1=" + X1 + 
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
		long t0 = System.currentTimeMillis();
		int n=0;
		int mod = lower.length / 100;
		for (int i = 0; i < lower.length; i++) {
			n += tree.query(lower[i], upper[i]);
			if (i%mod == 0 && System.currentTimeMillis()-t0 > 1000) System.out.print('.');
		}
		if (DEBUG) {
			log(TestPerf.toStringOut());
			TestPerf.resetStats();
		}
		return n;
	}

	static void log(String string) {
		System.out.println(string);
	}

	static void logNLF(String string) {
		System.out.print(string);
	}


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
		log(date() + "updates ...");

		long t00 = timer();
		int n;
		long t;
		//Use result count from first run as control value
		int control = -1;
		do {
			n = 0;
			t = 0;
			double[][] u = null; //2 points, 2 versions
			//TODO we actually use a different dataset for each run here,
			//     this makes it a bit less comparable if different indexes
			//     perform different number of runs. But this seems to be the
			//     (relatively) most accurate solution.
			int nUpdates = S.cfgUpdateSize > S.cfgNEntries/4 ? S.cfgNEntries/4 : S.cfgUpdateSize;
			for (int i = 0; i < S.cfgUpdateRepeat; i++) {
				//prepare query
				u = test.generateUpdates(nUpdates, data, u);
				JmxTools.reset();
				//updates
				long t1 = timer();
				n += tree.update(u);
				long t2 = timer();
				t += t2-t1;
				S.statGcDiffUp += JmxTools.getDiff();
				S.statGcTimeUp += JmxTools.getTime();
			}
			if (control == -1) {
				control = n;
			}
			logNLF("*");
		} while (toMS(t00, timer()) < minimumMS);

		log("Elements updated: " + n + " -> " + n);
		log("Update time: " + toMS(t) + " ms -> " + toNSPerOp(t, n) + " ns/update");
		if (round == 0) {
			S.statTu1 = (long) toMS(t);
			S.statTu1E = toNSPerOp(t, n);
			S.statPSu1E = opsPerSec(n, t);
			S.statNu1 = control;
		} else {
			S.statTu2 = (long) toMS(t);
			S.statTu2E = toNSPerOp(t, n);
			S.statPSu2E = opsPerSec(n, t);
			S.statNu2 = control;
		}
	}

	private void unload() {
		log("Unloading...");
		JmxTools.reset();

		long t1 = timer();
		int n = tree.unload();
		long t2 = timer();

		log("Deletion time: " + toMS(t1, t2) + " ms -> " + 
				toNSPerOp(t1, t2, S.cfgNEntries) + " ns/delete");
		S.statTUnload = (long) toMS(t1, t2);
		S.statPSUnload = opsPerSec(n, t1, t2);
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

	private String date() {
		return FT.format(new Date()) + " ";
	}

	public Candidate getCandidate() {
		return tree;
	}

	private static long timer() {
		return System.nanoTime();
	}

	private static long opsPerSec(int nOps, double t1, double t2) {
		return opsPerSec(nOps, t2-t1);
	}

	private static long opsPerSec(int nOps, double t) {
		return (long) (nOps / t * 1_000_000_000L);
	}

	private static double toMS(double t1, double t2) {
		return (t2-t1)/1_000_000;
	}

	private static double toMS(double t) {
		return t/1_000_000;
	}

	//	private static double toSec(double t1, double t2) {
	//		return toMS(t1, t2) * 1_000;
	//	}

	private static long toNSPerOp(double t1, double t2, long nOps) {
		return toNSPerOp(t2 - t1, nOps);
	}

	private static long toNSPerOp(double t, long nOps) {
		return (long) t / nOps;
	}
}
