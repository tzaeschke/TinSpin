/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.util.List;

import ch.ethz.globis.tinspin.TestStats.IDX;
import ch.ethz.globis.tinspin.TestStats.TST;
import ch.ethz.globis.tinspin.data.TestPoint;
import ch.ethz.globis.tinspin.util.rmi.TestManagerRMI;
import ch.ethz.globis.tinspin.util.rmi.TestProcessLauncher;
import ch.ethz.globis.tinspin.wrappers.Candidate;

public class TestManager {

	private static TestLogging log = new TestLogging();
	
	public static void main(String[] args) {
		TestProcessLauncher.launchRmiRegistry();
		long t1 = System.currentTimeMillis();
		try {
//			suiteDims(TST.CUBE, IDX.RSS2, 0.1, 1.0);
//			suiteDims(TST.CUBE, IDX.RSS, 0.1, 1.0);

			
//			suiteDims(TST.CUBE, IDX.PHC, 0.1, 1.0);
//			suiteDims(TST.CUBE, IDX.RSZ, 0.1, 1.0);

			double[] sizesData = {0.1, 0.5, 1, 5, 10, 25};
			testSeries(TST.CUBE, IDX.CBF, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.CBZ, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.PHC, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.PHC_IPP, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.RSS, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.RSZ, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.KD_LEVY, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.KD_SAVA, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.QKDZ, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.XTS, 3, 1.0, sizesData);
			testSeries(TST.CUBE, IDX.XTR, 3, 1.0, sizesData);

//			//sizesData = new double[]{0.1, 0.5, 1, 5, 10};
//			testSeries(TST.CLUSTER, IDX.CBF, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.CBZ, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC_IPP, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSS, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSZ, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.QKDZ, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.XTS, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.XTR, 3, 3.4, sizesData);

//			testSeries(TST.CUBE, IDX.PHC, 3, 1.0, 1, 2, 5, 10);
//			testSeries(TST.CUBE, IDX.PHC_IPP, 3, 1.0, 1, 2, 5, 10);
//			testSeries(TST.CUBE, IDX.RSS, 3, 1.0, 1, 2, 5, 10);
//			testSeries(TST.CUBE, IDX.RSZ, 3, 1.0, 1, 2, 5, 10);
//			testSeries(TST.CUBE, IDX.RSL, 3, 1.0, 1, 2, 5, 10);
//			testSeries(TST.CUBE, IDX.QKDZ, 3, 1.0, 1, 2, 5, 10);

			suiteDims(TST.CUBE, IDX.PHC, 1, 1.0);
			suiteDims(TST.CUBE, IDX.PHC_IPP, 1, 1.0);
			suiteDims(TST.CUBE, IDX.RSS, 1, 1.0);
			suiteDims(TST.CUBE, IDX.RSZ, 1, 1.0);
			suiteDims(TST.CUBE, IDX.QKDZ, 1, 1.0);
			suiteDims(TST.CUBE, IDX.KD_LEVY, 1, 1.0);
			suiteDims(TST.CUBE, IDX.KD_SAVA, 1, 1.0);
			suiteDims(TST.CUBE, IDX.CBZ, 1, 1.0);
			suiteDims(TST.CUBE, IDX.XTR, 1, 1.0);
//			suiteDims(TST.CLUSTER, IDX.PHC, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC_IPP, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.RSS, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.RSZ, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.QKDZ, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.KD_LEVY, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.KD_SAVA, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.CBZ, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.XTR, 1, 3.4);
//			
//
//			testSeriesWQSR(TST.CUBE, IDX.PHC, 3, 0.00001, 1, 1, 10, 100, 1000, 10000);
//////			testSeriesWQSR(TST.CUBE, IDX.PHC2, 3, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.PHC_IPP, 3, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.RSS, 3, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.RSZ, 3, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.XTR, 3, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.QKDZ, 3, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			
////			testSeriesWQS(TST.CUBE, IDX.PHC, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
////			testSeriesWQS(TST.CUBE, IDX.PHC2, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.QKDZ, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.PHC_IPP, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
////			testSeriesWQS(TST.CUBE, IDX.RSL, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
////			testSeriesWQS(TST.CUBE, IDX.KD_LEVY, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
////			testSeriesWQS(TST.CUBE, IDX.KD_SAVA, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
////			testSeriesWQS(TST.CUBE, IDX.RSS, 3, 1.0, 1, 1, 10, 100, 1000, 10000);
////			testSeriesWQS(TST.CUBE, IDX.XTR, 3, 1.0, 1, 1, 10, 100, 1000, 10000);
////			testSeriesWQS(TST.CUBE, IDX.MX_CIF, 3, 1.0, 1, 1, 10, 100, 1000, 10000);

			testSeriesR(TST.CUBE, IDX.PHC, 3, 1.0, 1, 2, 5, 10, 25);
			testSeriesR(TST.CUBE, IDX.PHC_IPP, 3, 1.0, 1, 2, 5, 10, 25);
			testSeriesR(TST.CUBE, IDX.RSZ, 3, 1.0, 1, 2, 5, 10, 25);
			testSeriesR(TST.CUBE, IDX.RSS, 3, 1.0, 1, 2, 5, 10, 25);
			testSeriesR(TST.CUBE, IDX.QKDZ, 3, 1.0, 1, 2, 5, 10, 25);
			testSeriesR(TST.CUBE, IDX.XTR, 3, 1.0, 1, 2, 5, 10, 25);

			suiteDimsR(TST.CUBE, IDX.PHC, 1, 1.0);
			suiteDimsR(TST.CUBE, IDX.PHC_IPP, 1, 1.0);
			suiteDimsR(TST.CUBE, IDX.RSS, 1, 1.0);
			suiteDimsR(TST.CUBE, IDX.RSZ, 1, 1.0);
			suiteDimsR(TST.CUBE, IDX.QKDZ, 1, 1.0);
			suiteDimsR(TST.CUBE, IDX.XTR, 1, 1.0);

//			testSeries(TST.CUBE, IDX.PHC, 3, 1.0, 1);
//			testSeries(TST.CUBE, IDX.PHC, 3, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.PHC, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.PHC, 10, 1.0);
//			testSeries(TST.CUBE, IDX.RSZ, 3, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.RSZ, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.RSZ, 10, 1.0);
//			testSeries(TST.CUBE, IDX.RSS, 3, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.RSS, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.RSS, 10, 1.0);
//			testSeries(TST.CUBE, IDX.PHC2, 3, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.PHC2, 10, 1.0);
//			testSeries(TST.CUBE, IDX.PHC_IPP, 3, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.PHC_IPP, 10, 1.0);
//			testSeries(TST.CUBE, IDX.KD_LEVY, 3, 1.0, 1, 2, 5, 10, 25, 50, 75);
//			suiteDims(TST.CUBE, IDX.KD_LEVY, 10, 1.0);
//			testSeries(TST.CUBE, IDX.KD_SAVA, 3, 1.0, 1, 2, 5, 10, 25, 50, 75);
//			suiteDims(TST.CUBE, IDX.KD_SAVA, 10, 1.0);
//			testSeries(TST.CLUSTER, IDX.PHC, 3, 3.4, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CLUSTER, IDX.PHC, 0.1, 3.4);
//			
//			testSeries(TST.CUBE, IDX.RSS, 3, 64, 1.0, 1, 2, 5, 10, 25);
//			testSeries(TST.CLUSTER, IDX.RSS, 3, 64, 3.4, 1, 2, 5, 10, 25);
//			suiteDims(TST.CUBE, IDX.RSS, 10, 1.0);
//			suiteDims(TST.CLUSTER, IDX.RSS, 0.1, 3.4);
////			testSeries(TST.CLUSTER, IDX.PHC2, 3, 64, 3.4, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CLUSTER, IDX.PHC2, 0.1, 3.4);
////			testSeries(TST.CLUSTER, IDX.PHC, 3, 64, 3.4, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CLUSTER, IDX.PHC, 0.1, 3.5);
////			testSeries(TST.CLUSTER, IDX.PHC2, 3, 64, 3.4, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CLUSTER, IDX.PHC2, 0.1, 3.5);
//			testSeries(TST.CLUSTER, IDX.PHCF, 3, 64, 3.4, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CLUSTER, IDX.PHCF, 10, 3.4);
//			testSeries(TST.CUBE, IDX.PHCF, 3, 64, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.PHCF, 10, 1.0);

//			testSeries(TST.CLUSTER, IDX.PHC, 3, 64, 3.4, 75, 100);
//			testSeries(TST.CLUSTER, IDX.PHC2, 3, 64, 3.4, 75, 100);
//			suiteDims(TST.CUBE, IDX.PHC2_IPP, 10, 1.0);
//			testSeries(TST.CUBE, IDX.PHC, 3, 64, 1.0, 1, 2, 5, 10, 15, 25, 50);//, 75, 100);
//			testSeries(TST.CUBE, IDX.PHC2, 3, 64, 1.0, 1, 2, 5, 10, 15, 25, 50);//, 75, 100);
			//suiteRange();
			//suiteTigerRange();
			//suiteTigerPoint();
		} finally {
			log.printSummary();
		}
		long t2 = System.currentTimeMillis();
		long t = t2-t1;
		System.out.println("Total time: " + t + "[ms] = " + t/1000 + "[s] = " + t/60000 + "[m]");
	}
	
	private static void suiteDims(TST test, IDX idx, double nEntries, double p1) {
		int n = (int) (nEntries*1000*1000);
		int r = 3;
		test(r, test, idx, 2, n, p1);
		test(r, test, idx, 3, n, p1);
		test(r, test, idx, 4, n, p1);
		test(r, test, idx, 5, n, p1);
		if (idx == IDX.RSS) {
			log.writeLogFileForTestSeries("dimsP");
			return;
		}
		test(r, test, idx, 6, n, p1);
		test(r, test, idx, 8, n, p1);
//		test(r, test, idx, 10, n, p1);
//		test(r, test, idx, 12, n, p1);
//		test(r, test, idx, 14, n, p1);
//		test(r, test, idx, 15, n, p1);

//		test(r, test, idx, 20, n, p1);
//		test(r, test, idx, 25, n, p1);
//		test(r, test, idx, 30, n, p1);
//		test(r, test, idx, 40, n, p1);
//		test(r, test, idx, 50, n, p1);
//		test(r, test, idx, 60, n, p1);
		log.writeLogFileForTestSeries("dimsP");
	}
	
	private static void suiteDimsR(TST test, IDX idx, double nEntries, double p1) {
		int n = (int) (nEntries*1000*1000);
		int r = 3;
		testR(r, test, idx, 2, n, p1);
		testR(r, test, idx, 3, n, p1);
		testR(r, test, idx, 4, n, p1);
		if (idx == IDX.RSS) {
			log.writeLogFileForTestSeries("dimsR");
			return;
		}
		testR(r, test, idx, 5, n, p1);
		testR(r, test, idx, 6, n, p1);
		testR(r, test, idx, 8, n, p1);
		testR(r, test, idx, 10, n, p1);
		testR(r, test, idx, 12, n, p1);
//		testR(r, test, idx, 15, n, p1);
//		testR(r, test, idx, 20, n, p1);
		log.writeLogFileForTestSeries("dimsR");
	}
		
	
	// ===== Custom Tests =====
	
	public TestManager() {
		TestProcessLauncher.launchRmiRegistry();
	}
	
	public void test(int repeat, TST TEST, IDX INDEX,
			int DIM, int N, double param1,
			Class<? extends TestPoint> testClass,
			Class<? extends Candidate> indexClass) { 
		for (int i = 0; i < repeat; i++) {
			test(TEST, INDEX, DIM, N, i, false, param1, testClass, indexClass);
		}
		if (repeat > 1) {
			log.logResultAverage(repeat);
			log.printSummary();
		}
	}
	
	public void test(TST TEST, IDX INDEX,
			int DIM, int N, long SEED, 
			boolean isRangeData, double param1,
			Class<? extends TestPoint> testClass,
			Class<? extends Candidate> indexClass) {
		TestStats s0 = new TestStats(TEST, INDEX, N, DIM, isRangeData, param1);
		s0.setSeed(SEED);
		s0.testClass = testClass;
		s0.indexClass = indexClass;
		//We use two different TestStats instances here, because the 2nd is returned from RMI.
		TestStats s = TestManagerRMI.runTest(s0);
		log.logResult(s);
		log.printSummary();
	}
	
	// ===== Standard Tests =====
	
	private static void testSeries(TST TEST, IDX INDEX, int DIM, double param1, double... Ns) {
		for (double N: Ns) {
			test(3, TEST, INDEX, DIM, (int)(N*1000*1000), param1);
		}
		log.writeLogFileForTestSeries("sizeP");
	}

	private static void test(int repeat, TST TEST, IDX INDEX, int DIM, int N, double param1) { 
		for (int i = 0; i < repeat; i++) {
			test(TEST, INDEX, DIM, N, i, false, param1);
		}
		if (repeat > 1) {
			log.logResultAverage(repeat);
			log.printSummary();
		}
	}

	/**
	 * Test varying result size for window queries.
	 * @param TEST
	 * @param INDEX
	 * @param DIM
	 * @param param1
	 * @param N
	 * @param wqss
	 */
	private static void testSeriesWQS(TST TEST, IDX INDEX, int DIM, double param1, double N, int...wqss) {
		for (int wqs: wqss) {
			TestStats ts = new TestStats(TEST, INDEX, (int)(N*1000*1000), DIM, false, param1);
			ts.cfgWindowQuerySize = wqs;
			runTest(ts);
		}
		log.writeLogFileForTestSeries("sizeP");
	}

	/**
	 * Range tests.
	 * @param TEST
	 * @param INDEX
	 * @param DIM
	 * @param DEPTH
	 * @param param1
	 * @param Ns
	 */
	private static void testSeriesR(TST TEST, IDX INDEX, int DIM, 
			double param1, int... Ns) {
		for (int N: Ns) {
			testR(3, TEST, INDEX, DIM, N*1000*1000, param1);
		}
		log.writeLogFileForTestSeries("sizeR");
	}

	private static void testR(int repeat, TST TEST, IDX INDEX, int DIM, 
			int N, double param1) { 
		for (int i = 0; i < repeat; i++) {
			test(TEST, INDEX, DIM, N, i, true, param1);
		}
		if (repeat > 1) {
			log.logResultAverage(repeat);
			log.printSummary();
		}
	}

	/**
	 * Test varying result size for window queries.
	 * @param TEST
	 * @param INDEX
	 * @param DIM
	 * @param param1
	 * @param N
	 * @param wqss
	 */
	private static void testSeriesWQSR(TST TEST, IDX INDEX, int DIM, 
			double param1, int N, int... wqss) {
		for (int wqs: wqss) {
			TestStats.DEFAULT_W_QUERY_SIZE = wqs;
			testR(3, TEST, INDEX, DIM, N*1000*1000, param1);
		}
		log.writeLogFileForTestSeries("sizeR");
	}

	
	private static void test(TestStats.TST TEST, IDX INDEX, int DIM, int N, long SEED, 
			boolean isRangeData, double param1) {
		TestStats s0 = new TestStats(TEST, INDEX, N, DIM, isRangeData, param1);
		s0.setSeed(SEED);
		//We use two different TestStats instances here, because the 2nd is returned from RMI.
		TestStats s = TestManagerRMI.runTest(s0);
		log.logResult(s);
		log.printSummary();
	}

	/**
	 * Run the provided test remotely in a separate process.
	 * The averaged results are returned in a separate instance.
	 * @param ts test configuration
	 * @return results
	 */
	public static TestStats runTest(TestStats ts) {
		for (int i = 0; i < ts.cfgNRepeat; i++) {
			ts.setSeed(i);
			//We use two different TestStats instances here, because the 2nd is returned from RMI.
			TestStats s = TestManagerRMI.runTest(ts);
			log.logResult(s);
			log.printSummary();
		}
		TestStats avg = log.logResultAverage(ts.cfgNRepeat);
		log.printSummary();
		return avg;
	}

	/**
	 * Run the provided test remotely in a separate process.
	 * The averaged results are returned in a separate instance.
	 * @param tsList test configurations
	 * @return results
	 */
	public static List<TestStats> runTests(List<TestStats> tsList) {
		for (TestStats ts: tsList) {
			runTest(ts);
		}
		log.printSummary();
		return log.getAvgStats();
	}

}
