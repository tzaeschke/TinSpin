/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import ch.ethz.globis.phtree.PhTreeHelper;
import ch.ethz.globis.phtree.demo.PointCBF;
import ch.ethz.globis.phtree.demo.PointCBR;
import ch.ethz.globis.phtree.demo.PointCompactHilbert;
import ch.ethz.globis.phtree.demo.PointCritBitZ;
import ch.ethz.globis.phtree.demo.PointKDL;
import ch.ethz.globis.phtree.demo.PointKDRed;
import ch.ethz.globis.phtree.demo.PointKDS;
import ch.ethz.globis.phtree.demo.PointMXCIFKD;
import ch.ethz.globis.phtree.demo.PointPHC;
import ch.ethz.globis.phtree.demo.PointPHC2;
import ch.ethz.globis.phtree.demo.PointPHC2_IPP;
import ch.ethz.globis.phtree.demo.PointPHCCTree;
import ch.ethz.globis.phtree.demo.PointPHCF;
import ch.ethz.globis.phtree.demo.PointPHCRectangle;
import ch.ethz.globis.phtree.demo.PointPHC_IPP;
import ch.ethz.globis.phtree.demo.PointPHC_PHE;
import ch.ethz.globis.phtree.demo.PointPHC_v1;
import ch.ethz.globis.phtree.demo.PointPRTree;
import ch.ethz.globis.phtree.demo.PointQuadZ;
import ch.ethz.globis.phtree.demo.PointRSLokeshj;
import ch.ethz.globis.phtree.demo.PointRStarSeeger;
import ch.ethz.globis.phtree.demo.PointXtree;
import ch.ethz.globis.phtree.demo.RectangleMXCIF2D;
import ch.ethz.globis.phtree.demo.RectangleMXCIF3D;
import ch.ethz.globis.phtree.demo.RectanglePHC;
import ch.ethz.globis.phtree.demo.RectanglePHC2;
import ch.ethz.globis.phtree.demo.RectanglePHCF;
import ch.ethz.globis.phtree.demo.RectanglePHC_IPP;
import ch.ethz.globis.phtree.demo.RectanglePRT;
import ch.ethz.globis.phtree.demo.RectangleQuadZ;
import ch.ethz.globis.phtree.demo.RectangleRStarSeeger;
import ch.ethz.globis.phtree.demo.RectangleXtree;
import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.phtree.util.JmxTools;
import ch.ethz.globis.phtree.util.TestPerf;
import ch.ethz.globis.phtree.util.Tools;
import ch.ethz.globis.tinspin.data.points.TestPoint;
import ch.ethz.globis.tinspin.data.rectangles.TestRectangle;


/**
 * Performs tests as described in:
 * [1] L. Arge, M. de Berg, H. J. Haverkort and K. Yi: 
 * "The Priority R-Tree: A Practically Efficient and Worst-Case Optimal R-Tree"
 *
 * @author Tilmann Zaschke
 */
public class MainTest {
	
	private static final SimpleDateFormat FT = new SimpleDateFormat ("yyyy-MM-dd' 'HH:mm:ss");

	private final TestStats S;
	
	private final Random R;
	
	private static final int N_RANGE_QUERY = 1000; //number of range queries
	//private static int N_RESULT_PER_QUERY = 1000; //average expected results per range query
	private static final int N_POINT_QUERY = 1000*1000; //number of point queries
	private static final int N_KNN_QUERY = 10*100;
	private static final int N_UPDATES = 100*1000;
	private static final int N_UPDATE_CYCLES = 10;
	
	public static boolean USE_NEW_QUERIES = true;
	
	//private PersistenceManager pm;
	private double[] data;
	private Candidate tree;
	
	// Added by Adrien 06/02/2014 to support queries for CUSTOM datasets
	private TestPoint customTest;
	
	/** Edge length of the populated data area. */
	//private final double LEN = (1L<<31)-1;
	//private final double LEN = 1000.0;
	private final double LEN = 1.0;
	
	private static final boolean DEBUG = PhTreeHelper.DEBUG;
	
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
		final int DEPTH = 64;
		final int N = 1*10*1000;
						
		System.err.println("KNN count = " + N_KNN_QUERY); //TODO
		TestStats s0 = new TestStats(TestStats.TST.CUBE, TestStats.IDX.QKDZ, N, DIM, DEPTH, true, 0.00001);
		//TestStats s0 = new TestStats(TST.CLUSTER, IDX.PHC, N, DIM, DEPTH, false, 3.4);
		//TestStats s0 = new TestStats(TST.CUBE, IDX.QKDZ, N, DIM, DEPTH, false, 1.0);
		s0.setSeed(0);
		MainTest test = new MainTest(s0);
		TestStats s = test.run();
		System.out.println(s);
		//System.out.println(BitsLong.POOL.print());
//		System.out.println("AMM: " + PhIteratorNoGC.AMM1 + " / " + PhIteratorNoGC.AMM2 + " / " + PhIteratorNoGC.AMM3 + " / " + PhIteratorNoGC.AMM4 + " / " + PhIteratorNoGC.AMM5 + " / ");
//		System.out.println("VMM: " + PhIteratorNoGC.VMM1 + " / " + PhIteratorNoGC.VMM2 + " / " + PhIteratorNoGC.VMM3);
//		System.out.println("HCI-A/L/N: " + PhIteratorNoGC.HCIA + " / " + PhIteratorNoGC.HCIL + " / " + PhIteratorNoGC.HCIN);
	}
	
	private static void runWithArgs(String[] args) {
		if (args.length < 4) {
			System.out.println("ERROR: At least 4 arguemnts required, found: " + args.length);
			System.out.println("Example: MainTest CUBE PHC 1000000 3 <64/32/...> <true/false> "
					+ "<1.0/3.4/3.5/...> <0/1/2/...>");
			System.out.println("Example: MainTest [TEST] [INDEX] [SIZE] [DIM] [BITS=64] "
					+ "[BOXES=false] [param=1.0] [random seed=0]");
			return;
		}
		TestStats.TST tst;
		try {
			tst = TestStats.TST.valueOf(args[0]);
		} catch (IllegalArgumentException e) {
			System.out.println("Test not recognised: " + args[0]);
			System.out.print("Please choose one of: ");
			for (TestStats.TST t: TestStats.TST.values()) {
				System.out.print(t.name() + ", ");
			}
			return;
		}
		
		TestStats.IDX idx;
		try {
			idx = TestStats.IDX.valueOf(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("Index not recognised: " + args[0]);
			System.out.print("Please choose one of: ");
			for (TestStats.IDX t: TestStats.IDX.values()) {
				System.out.print(t.name() + ", ");
			}
			return;
		}
		
		int n = Integer.parseInt(args[2]);
		int dim = Integer.parseInt(args[3]);
		int depth = args.length > 4 ? Integer.parseInt(args[4]) : 64;
		boolean box = args.length > 5 ? Boolean.parseBoolean(args[5]) : false;
		double param0 = args.length > 6 ? Double.parseDouble(args[6]) : 1.0;
		int seed = args.length > 7 ? Integer.parseInt(args[7]) : 0;

		TestStats s0 = new TestStats(tst, idx, n, dim, depth, box, param0);
		s0.setSeed(seed);
		MainTest test = new MainTest(s0);
		TestStats s = test.run();
		System.out.println(s);
		return;
	}

	public MainTest(TestStats S) { 
		this.S = S;
		this.R = new Random(S.seed);
	}
	
	public TestStats run() {
		
		final boolean useDB = false; 
		
		openDB(useDB);
		JmxTools.startUp();

		load(S);
		
//		if (true) {
//			TestDraw.draw(data, DIM);
//			//return S;
//		}
				
//		if (true) return S;
		
//		commitDB();
//		reportDB();
		
		repeatQuery(N_RANGE_QUERY);
		repeatQuery(N_RANGE_QUERY);
		S.assortedInfo += " WINDOW_RESULTS=" + S.paramWQSize;
		
		//perform point queries.
		repeatPointQuery(N_POINT_QUERY);
		repeatPointQuery(N_POINT_QUERY);
		
		if (tree.supportsKNN()) {
			int repeat = getKnnRepeat(S.statNDims);
			S.assortedInfo += " KNN_REPEAT=" + repeat;
			repeatKnnQuery(repeat, 1);
			repeatKnnQuery(repeat, 1);
			repeatKnnQuery(repeat, 10);
			repeatKnnQuery(repeat, 10);
		}
		
		if (tree.supportsUpdate()) {
			update();
			update();
		} else {
			System.err.println("WARNING: update() disabled");
		}
		
		unload();

		if (tree != null) {
			tree.release();
		}
		
//		reportDB();
//		closeDB();
		
		return S;
	} 
	
	private int getKnnRepeat(int dims) {
		if (S.TEST == TestStats.TST.CLUSTER) {
			return 20;
		}
		if (dims <= 3) {
			return N_KNN_QUERY;
		}
		if (dims <= 6) {
			return N_KNN_QUERY/10;
		}
		if (dims <= 10) {
			return N_KNN_QUERY/10;
		}
		return N_KNN_QUERY/50;
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
		case TIGER:
		case TOUCH:
		case VORTEX: {
			data = test.generate();
			break;
		}
		//case ASPECT:
		case MBR_SIZE: {
			//IS_POINT_DATA = PR_TestSize.generate(R, LEN, N, DIM, 0.001f);
			//IS_POINT_DATA = PR_TestSize.generate(R, LEN, N, DIM, 0.02f);
			//data = PR_TestAspect.generate(R, LEN, N, DIM, 1e3f);//10.0f);
			data = test.generate();
			if (!ts.isRangeData) throw new IllegalStateException();
			break;
		}
		case CUSTOM: {
			if (S.testClass == null)
				throw new RuntimeException("No dataset class passed (null)");
			
			try {
				// Note: a custom Test class MUST have an empty constructor
				customTest = S.testClass.getDeclaredConstructor().newInstance();
				test = customTest;
				data = customTest.generate();
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
		
		int dims = S.statNDims;
		int N = S.statNEntries;
		
        long memTree = Tools.getMemUsed();
		Tools.cleanMem(N, memTree);

		
		//load index
		log(time() + "loading index ...");
        memTree = Tools.getMemUsed();
        JmxTools.reset();
		long t1 = System.currentTimeMillis();
		
		if (S.isRangeData) {
			tree = createRangeTree(ts);
		} else {
			tree = createPointTree(ts);
		}
		tree.setIndexType(ts.INDEX);
		tree.load(data, dims);

		long t2 = System.currentTimeMillis();
		S.statGcDiffL = JmxTools.getDiff();
		S.statGcTimeL = JmxTools.getTime();
		log("loading finished in: " + (t2-t1));
		S.statSjvmF = Tools.cleanMem(N, memTree);
		S.statSjvmE = S.statSjvmF / N;
		S.statTLoad = t2-t1;
		
		if (ts.INDEX == TestStats.IDX.PHCC) {
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

	private Candidate createPointTree(TestStats ts) {
		int nEntries = ts.statNEntries;
		int dims = ts.statNDims;
		switch (ts.INDEX) {
		case HIL: return PointCompactHilbert.create(nEntries, dims);
		case PHCv1: return new PointPHC_v1(nEntries, dims);
		case PHC: return new PointPHC(ts);
		case PHC2: return new PointPHC2(nEntries, dims);
		case PHCF: return new PointPHCF(nEntries, dims);
		case PHC_PHE: return new PointPHC_PHE(nEntries, dims);
		case PHC_IPP: return new PointPHC_IPP(nEntries, dims);
		case PHC2_IPP: return new PointPHC2_IPP(nEntries, dims);
		case PHCC: return new PointPHCCTree(nEntries, dims);
		case PHC_RECTANGLE: return new PointPHCRectangle(ts);
//		case MX_CIF: 
//			if (dims == 3) {
//				return new PointMXCIF3D(nEntries, dims);
//			} else {
//				throw new UnsupportedOperationException();
//			}
		case MX_CIF: return new PointMXCIFKD(nEntries, dims);
		case CBZ: return PointCritBitZ.create(dims, nEntries);
		case PRT: return PointPRTree.create(dims, nEntries);
		case KD_SAVA: return PointKDS.create(dims, nEntries);
		case KD_LEVY: return PointKDL.create(dims, nEntries);
		case KD_RED: return PointKDRed.create(dims, nEntries);
		case RSL: return PointRSLokeshj.create(dims, nEntries);
		case RSS: return PointRStarSeeger.create(dims, nEntries);
		case XTR: return PointXtree.create(dims, nEntries);
		case CB: return PointCBR.create(dims, nEntries);
		case CBF: return PointCBF.create(dims, nEntries);
		case QKDZ: return new PointQuadZ(nEntries, dims);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private Candidate createRangeTree(TestStats ts) {
		int nEntries = ts.statNEntries;
		int dims = ts.statNDims;
		switch (ts.INDEX) {
		case PHC: return new RectanglePHC(nEntries, dims);
		case PHC2: return new RectanglePHC2(nEntries, dims);
		case PHCF: return new RectanglePHCF(nEntries, dims);
		case QKDZ: return new RectangleQuadZ(nEntries, dims);
		case MX_CIF: 
			if (dims == 2) {
				return new RectangleMXCIF2D(nEntries, dims);
			} else if (dims == 3) {
				return new RectangleMXCIF3D(nEntries, dims);
			} else {
				throw new UnsupportedOperationException();
			}
		case PHC_IPP: return new RectanglePHC_IPP(nEntries, dims);
		case PRT: return RectanglePRT.create(dims, nEntries);
		case RSS: return RectangleRStarSeeger.create(dims, nEntries);
		case XTR: return RectangleXtree.create(dims, nEntries);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private void openDB(boolean useDB) {
		if (!useDB) {
			return;
		}
		
//		//enabled statistics
//		DBStatistics.enable(true);
//		
//		ZooConfig.setFileManager(ZooConfig.FILE_MGR_IN_MEMORY);
//		String dbName = "pr_main";
//		if (!ZooHelper.getDataStoreManager().dbExists(dbName)) {
//			log("Creating database");
//			ZooHelper.getDataStoreManager().createDb(dbName);
//		} else {
//			log("Removing database");
//			ZooHelper.getDataStoreManager().removeDb(dbName);
//			log("Creating database");
//			ZooHelper.getDataStoreManager().createDb(dbName);
//		}
//		ZooJdoProperties props = new ZooJdoProperties(dbName);
//		//props.setRetainValues(true);
//		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(props);
//		pm = pmf.getPersistenceManager();
//		pm.currentTransaction().begin();
	}
	
	
//	private void commitDB() {
//		if (pm == null) {
//			return;
//		}
//		
//		if (phc != null) {
//			pm.makePersistent(phc);
//		} else if (prt != null) {
//			pm.makePersistent(prt);
//		} else {
//			throw new IllegalStateException();
//		}
//		pm.currentTransaction().commit();
//		pm.currentTransaction().begin();
//		pm.evictAll(true, LeafNode.class);
//
//		//		Object oid = JDOHelper.getObjectId(idx);
////		//pm.evictAll();
////		
////		PersistenceManagerFactory pmf = pm.getPersistenceManagerFactory();
////		pm.currentTransaction().rollback();
////		pm.close();
////		pm = pmf.getPersistenceManager();
////		pm.currentTransaction().begin();
////		return (PhTree) pm.getObjectById(oid);
//		
//		
//		if (INDEX == IDX.PRT) {
//			Extent<LeafNode> ext1 = pm.getExtent(LeafNode.class);
//			int nL = 0;
//			Iterator<LeafNode> it1 = ext1.iterator();
//			while (it1.hasNext()) {
//				nL++;
//				it1.next();
//			}
//			Extent<InternalNode> ext2 = pm.getExtent(InternalNode.class);
//			int nI = 0;
//			Iterator<InternalNode> it2 = ext2.iterator();
//			while (it2.hasNext()) {
//				nI++;
//				it2.next();
//			}
//			log("Leaves: " + nL);
//			log("Inner: " + nI);
//			pm.currentTransaction().commit();
//			pm.currentTransaction().begin();
//			pm.evictAll(true, LeafNode.class);
//		}
//	}
//	
//	private void reportDB() {
//		if (pm == null) {
//			log("No DB was used.");
//			return;
//		}
//		DBStatistics stats = ZooJdoHelper.getStatistics(pm);
//		int rc = stats.getStoragePageReadCount();
//		int urc = stats.getStoragePageReadCountUnique();
//		int drc = stats.getStorageDataPageReadCount();
//		int udrc = stats.getStorageDataPageReadCountUnique();
//		int wc = stats.getStoragePageWriteCount();
//		log("Page size: " + ZooConfig.getFilePageSize());
//		log("Page read count: " + rc);
//		log("Page read count unique: " + urc);
//		log("Data page read count: " + drc);
//		log("Data page read count unique: " + udrc);
//		log("Page write count: " + wc);
//		log("Data pages: " + stats.getStat(STATS.DB_PAGE_CNT_DATA));
//	}
//	
//	private void closeDB() {
//		if (pm == null) {
//			return;
//		}
//		pm.currentTransaction().rollback();
//		pm.close();
//		pm.getPersistenceManagerFactory().close();
//		pm = null;
//	}
	
	private void repeatQuery(int repeat) {
		int dims = S.statNDims;
		log("N=" + S.statNEntries);
		log(time() + "querying index ... repeat = " + repeat);
		double[][] lower = new double[repeat][dims]; 
		double[][] upper = new double[repeat][dims];
		generateWindowQueries(lower, upper);
		JmxTools.reset();
		long t1 = System.currentTimeMillis();
		int n = 0;
		if (S.statNEntries < 10000 || !(tree.isOfType(TestStats.IDX.CB, TestStats.IDX.CBF))) {
			n = repeatQueries(lower, upper);
		} else {
			n = -1;
		}
		long t2 = System.currentTimeMillis();
		log("Query time: " + (t2-t1) + " ms -> " + (t2-t1)/(double)repeat + " ms/q -> " +
				(t2-t1)*1000*1000/(double)n + " ns/q/r  (n=" + n + ")");
		if (S.statTq1 == 0) {
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
	
	private void repeatPointQuery(int repeat) {
		log(time() + "point queries ...");
		//prepare query
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO return only double[], convert inside query function!
		//TODO
		//TODO
		//TODO
		Object q = preparePointQuery(repeat);
		q = tree.preparePointQuery((double[][]) q);
		JmxTools.reset();
		
		//query
		long t1 = System.currentTimeMillis();
		int n = tree.pointQuery(q);
		long t2 = System.currentTimeMillis();
		log("Elements found: " + n + " -> " + n/(double)repeat);
		log("Query time: " + (t2-t1) + " ms -> " + (t2-t1)/(double)repeat + " ms/q -> " +
				(t2-t1)*1000*1000/(double)n + " ns/q/r");
		if (S.statTqp1 == 0) {
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
		int dims = S.statNDims;
		double[][] qA;
		if (!S.isRangeData) {
			qA = new double[repeat][];
			for (int i = 0; i < repeat; i++) {
				qA[i] = generateQueryPointD(S.statNEntries, dims);
			}
		} else {
			qA = new double[repeat*2][];
			for (int i = 0; i < repeat; i++) {
				double[] lo = new double[dims];
				double[] hi = new double[dims];
				generateQueryPointDRect(lo, hi, S.statNEntries, dims);
				qA[2*i] = lo;
				qA[2*i+1] = hi;
			}
		}
		return qA;
	}

	private void repeatKnnQuery(int repeat, int k) {
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
			if (S.statTqk1_1 == 0) {
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
			if (S.statTqk10_1 == 0) {
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
		int dims = S.statNDims;
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
	
	private void generateWindowQueries(double[][] lower, double[][] upper) {
		for (int i = 0; i < lower.length; i++) {
			generateQueryCorners(lower[i], upper[i]);
		}
	}
	
	private int repeatQueries(double[][] lower, double[][] upper) {
		int n=0;
		for (int i = 0; i < lower.length; i++) {
			n += tree.query(lower[i], upper[i]);
			if (i%10 == 0) System.out.print('.');
		}
		System.out.println();
		MainTest.log("n=" + n/(double)lower.length);
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
	private void generateQueryCorners(double[] min, double[] max) {
		if (test.getTestType() == TestStats.TST.CLUSTER) {
			test.queryCuboid(S.paramWQSize, min, max);
			return;
		} else if (test.getTestType() == TestStats.TST.TIGER) {
			if (test.S.isRangeData) {
				test.queryCuboid(S.paramWQSize, min, max);
			}
			return;
		} else if (test.getTestType() == TestStats.TST.CUSTOM) {
			customTest.queryCuboid(S.paramWQSize, min, max);
			return;
		}

		if (USE_NEW_QUERIES) {
			generateQueryCornersNew(min, max);
		} else {
			generateQueryCornersOld(min, max);
		}
	}
	
	private void generateQueryCornersNew(double[] min, double[] max) {
		int dims = min.length;
		
		int nEntries = S.statNEntries;
		if (nEntries < S.paramWQSize*10) {
			//N < 10*000 ? -> N = 100
			nEntries = S.paramWQSize*10;
		}
		
		//Here is a fixed size version, returning 1% of the space.
		//final double qVolume = 0.01 * Math.pow(LEN, DIM);//(float) Math.pow(0.1, DIM); //0.01 for DIM=2
		final double avgVolume = S.paramWQSize/(double)nEntries * Math.pow(LEN, dims);
		//final double avgLen = Math.pow(avgVolume, 1./DIM);
		//final double avgLenVar = 0.5*avgLen;
		//final double minLen = 0.5*avgLen;
		
		//query create cube
		double[] len = new double[min.length];
		int nTries = 0;
		do {
			double vol = 1;
			for (int d = 0; d < dims-1; d++) {
				//calculate the average required len 
				final double avgLen = Math.pow(avgVolume/vol, 1./dims);
				//create a len between 0.5 and 1.5 of the required length
				len[d] = (0.5*avgLen) + R.nextDouble()*(avgLen);
				len[d] = len[d] > LEN*0.99 ? LEN*0.99 : len[d];
				vol *= len[d];
			}
			//create cuboid/box of desired size by dropping random length
			len[dims-1] = avgVolume/vol;  //now the new len creates a rectangle/box of SIZE.
			if (nTries++ > 100) {
				System.out.println(Arrays.toString(len) + " vol=" + vol + " aVol=" + avgVolume);
				throw new IllegalStateException("dims=" + dims + "  N=" + S.statNEntries);
			}
		} while (len[dims-1] >= LEN); //drop bad rectangles
		
		
		//create location
		for (int d = 0; d < dims; d++) {
			min[d] = R.nextDouble()*(LEN-len[d]);
			max[d] = min[d]+len[d];
			if (min[d]+len[d] >= LEN) {
				//drop bad rectangles 
				throw new RuntimeException();
			}
		}
		
		shuffle(len);
	}

	private void shuffle(double[] da) {
		// Fisher–Yates shuffle
		for (int i = da.length - 1; i > 0; i--) {
			int index = R.nextInt(i + 1);
			double a = da[index];
			da[index] = da[i];
			da[i] = a;
		}
	}

	private void generateQueryCornersOld(double[] min, double[] max) {
		int dims = min.length;
		//Here is a fixed size version, returning 1% of the space.
		//final double qVolume = 0.01 * Math.pow(LEN, DIM);//(float) Math.pow(0.1, DIM); //0.01 for DIM=2
		final double qVolume = S.paramWQSize/(double)S.statNEntries * Math.pow(LEN, dims);
		
		int dDrop = R.nextInt(dims);
		//query create cube
		double[] len = new double[min.length];
		do {
			double vol = 1;
			for (int d = 0; d < dims; d++) {
				if (d == dDrop) {
					continue;
				}
				len[d] = R.nextDouble()*LEN;
				vol *= len[d];
			}
			//create cuboid/box of desired size by dropping random length
			len[dDrop] = qVolume/vol;  //now the new len creates a rectangle/box of SIZE.
		} while (len[dDrop] >= LEN); //drop bad rectangles
		
		//create location
		for (int d = 0; d < dims; d++) {
			min[d] = R.nextDouble()*(LEN-len[d]);
			max[d] = min[d]+len[d];
			if (min[d]+len[d] >= LEN) {
				//drop bad rectangles 
				throw new RuntimeException();
			}
		}
	}

	
	private double[] generateQueryPointD(final int N, final int dims) {
		double[] xyz = new double[dims];
		int pos = R.nextInt(N*2);
		if (pos >= N) {
			//randomise
			for (int d = 0; d < dims; d++) {
				xyz[d] = R.nextDouble()*LEN;
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
				lo[d] = R.nextDouble()*LEN;
				hi[d] = lo[d] + R.nextDouble();
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
			xyz[d] = R.nextDouble()*LEN;
		}
		return xyz;
	}
	
	private void generateKnnQueryPointDRect(double[] lo, double[] hi, final int dims) {
		//randomise
		for (int d = 0; d < dims; d++) {
			lo[d] = R.nextDouble()*LEN;
			hi[d] = lo[d] + R.nextDouble();
		}
	}
	
	private void update() {
		log(time() + "updates ...");
		
		int n = 0;
		long t = 0;
		double[][] u = null; //2 points, 2 versions
		int nUpdates = N_UPDATES > S.statNEntries/4 ? S.statNEntries/4 : N_UPDATES;
		for (int i = 0; i < N_UPDATE_CYCLES; i++) {
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
		if (S.statTu1 == 0) {
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
		(t2-t1)*1000*1000/(double)S.statNEntries + " ns/q/r");
		S.statTUnload = t2-t1;
		S.statGcDiffUl = JmxTools.getDiff();
		S.statGcTimeUl = JmxTools.getTime();
		if (S.statNEntries != n) {
			throw new IllegalStateException("N/n: " + S.statNEntries + "/" + n);
		}
	}
	

	/**
	 * Float to long.
	 * @param f
	 * @return long.
	 */
	static long f2l(double f) {
		return BitTools.toSortableLong(f);
	}

	static void f2l(double[] f, long[] l) {
		BitTools.toSortableLong(f, l);
	}

	/**
	 * Float to long.
	 * @param f
	 * @return long.
	 */
	static double l2f(long l) {
		return BitTools.toDouble(l);
	}

	static void l2f(long[] l, double[] f) {
		BitTools.toDouble(l, f);
	}

	static double dist(double[] a, double[] b) {
		double dist = 0;
		for (int i = 0; i < a.length; i++) {
			double d =  a[i]-b[i];
			dist += d*d;
		}
		return Math.sqrt(dist);
	}
	
	static double distR(double[] center, double[] rLower, double[] rUpper) {
		double dist = 0;
		for (int i = 0; i < center.length; i++) {
			double d =  center[i]-(rUpper[i]-rLower[i])/2;
			dist += d*d;
		}
		return Math.sqrt(dist);
	}
	
	public TestStats getTestStats() {
		return S;
	}
	
	private String time() {
		return FT.format(new Date()) + " ";
	}
}
