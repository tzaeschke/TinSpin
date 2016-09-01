/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.globis.tinspin.data.points.TestPoint;
import ch.ethz.globis.tinspin.util.rmi.TestManagerRMI;
import ch.ethz.globis.tinspin.util.rmi.TestProcessLauncher;

public class MainTestManager {

	private static final ArrayList<TestStats> stats = new ArrayList<>();
	private static final ArrayList<TestStats> avgStats = new ArrayList<>();
	
	public static void main(String[] args) {
		TestProcessLauncher.launchRmiRegistry();
		long t1 = System.currentTimeMillis();
		try {
//			suiteDims(TST.CLUSTER, IDX.RSL, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.RSL, 10, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC, 10, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC2, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC2, 10, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC_IPP, 10, 3.5);
//			suiteDims(TST.CLUSTER, IDX.PHC2_IPP, 10, 3.5);
//			suiteDims(TST.CLUSTER, IDX.PHCF, 10, 3.5);
//			testSeries(TST.CUBE, IDX.PHC, 3, 64, 1.0, 0.5); //warmup
			
//			testSeries(TST.CUBE, IDX.PHC, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0); 
//			testSeries(TST.CUBE, IDX.PHC2, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0);
			testSeries(TestStats.TST.CUBE, TestStats.IDX.QKDZ, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0);
			testSeries(TestStats.TST.CUBE, TestStats.IDX.PHC_IPP, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0);
//			testSeries(TST.CUBE, IDX.RSS, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0); 
//			testSeries(TST.CUBE, IDX.XTR, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0); 
//			testSeries(TST.CUBE, IDX.KD_LEVY, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0);
//			testSeries(TST.CUBE, IDX.KD_SAVA, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0);
//			testSeries(TST.CUBE, IDX.CBZ, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0);
//			testSeries(TST.CUBE, IDX.MX_CIF, 3, 64, 1.0, 0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0);

//			testSeriesWQSR(TST.CUBE, IDX.PHC, 3, 64, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.PHC2, 3, 64, 0.00001, 1, 1, 10, 100, 1000, 10000);
			testSeriesWQSR(TestStats.TST.CUBE, TestStats.IDX.PHC_IPP, 3, 64, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.RSS, 3, 64, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.XTR, 3, 64, 0.00001, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQSR(TST.CUBE, IDX.MX_CIF, 3, 64, 0.00001, 1, 1, 10, 100, 1000, 10000);
			
//			testSeriesWQS(TST.CUBE, IDX.PHC, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.PHC2, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
			testSeriesWQS(TestStats.TST.CUBE, TestStats.IDX.QKDZ, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
			testSeriesWQS(TestStats.TST.CUBE, TestStats.IDX.PHC_IPP, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.RSL, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.KD_LEVY, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.KD_SAVA, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.RSS, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.XTR, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);
//			testSeriesWQS(TST.CUBE, IDX.MX_CIF, 3, 64, 1.0, 1, 1, 10, 100, 1000, 10000);

////			//testSeriesR(TST.CUBE, IDX.PHC, 3, 64, 0.01, 1, 2, 5, 10, 25);
////			testSeriesR(TST.CUBE, IDX.PHC, 3, 64, 0.001, 1, 2, 5, 10, 25);
//			testSeriesR(TST.CUBE, IDX.PHC, 3, 64, 0.00001, 1, 2, 5, 10, 25);
////			testSeriesR(TST.CUBE, IDX.PHC2, 3, 64, 0.001, 1, 2, 5, 10, 25);
//			testSeriesR(TST.CUBE, IDX.PHC2, 3, 64, 0.00001, 1, 2, 5, 10, 25);
////			//testSeriesR(TST.CUBE, IDX.RSS, 3, 64, 0.01, 1, 2, 5, 10, 25);
////			testSeriesR(TST.CUBE, IDX.RSS, 3, 64, 0.001, 1, 2, 5, 10, 25);
//			testSeriesR(TST.CUBE, IDX.RSS, 3, 64, 0.00001, 1, 2, 5, 10, 25);
////			//testSeriesR(TST.CUBE, IDX.XTR, 3, 64, 0.01, 1, 2, 5, 10, 25);
////			testSeriesR(TST.CUBE, IDX.XTR, 3, 64, 0.001, 1, 2, 5, 10, 25);
//			testSeriesR(TST.CUBE, IDX.XTR, 3, 64, 0.00001, 1, 2, 5, 10, 25);
//
//			
//			//suiteDimsR(TST.CUBE, IDX.PHC, 1, 0.01);
////			suiteDimsR(TST.CUBE, IDX.PHC, 1, 0.001);
//			suiteDimsR(TST.CUBE, IDX.PHC, 1, 0.00001);
////			suiteDimsR(TST.CUBE, IDX.PHC2, 1, 0.001);
//			suiteDimsR(TST.CUBE, IDX.PHC2, 1, 0.00001);
////			//suiteDimsR(TST.CUBE, IDX.RSS, 1, 0.01);
////			suiteDimsR(TST.CUBE, IDX.RSS, 1, 0.001);
//			suiteDimsR(TST.CUBE, IDX.RSS, 1, 0.00001);
////			//suiteDimsR(TST.CUBE, IDX.XTR, 1, 0.01);
////			suiteDimsR(TST.CUBE, IDX.XTR, 1, 0.001);
//			suiteDimsR(TST.CUBE, IDX.XTR, 1, 0.00001);

//			testSeries(TST.CUBE, IDX.PHC, 3, 64, 1.0, 1);
			testSeries(TestStats.TST.CUBE, TestStats.IDX.QKDZ, 3, 64, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
			suiteDims(TestStats.TST.CUBE, TestStats.IDX.QKDZ, 1, 1.0);
			suiteDims(TestStats.TST.CUBE, TestStats.IDX.QKDZ, 10, 1.0);
//			testSeries(TST.CUBE, IDX.PHC2, 3, 64, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.PHC2, 10, 1.0);
//			testSeries(TST.CUBE, IDX.PHC_IPP, 3, 64, 1.0, 1, 2, 5, 10, 25, 50, 75, 100);
//			suiteDims(TST.CUBE, IDX.PHC_IPP, 10, 1.0);
//			testSeries(TST.CUBE, IDX.KD_LEVY, 3, 64, 1.0, 1, 2, 5, 10, 25, 50, 75);
//			suiteDims(TST.CUBE, IDX.KD_LEVY, 10, 1.0);
//			testSeries(TST.CUBE, IDX.KD_SAVA, 3, 64, 1.0, 1, 2, 5, 10, 25, 50, 75);
//			suiteDims(TST.CUBE, IDX.KD_SAVA, 10, 1.0);
//			testSeries(TST.CLUSTER, IDX.PHC, 3, 64, 3.4, 1, 2, 5, 10, 25, 50, 75, 100);
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
			printSummary();
		}
		long t2 = System.currentTimeMillis();
		long t = t2-t1;
		System.out.println("Total time: " + t + "[ms] = " + t/1000 + "[s] = " + t/60000 + "[m]");
	}
	
	private static void suiteDims() {
		//cube
		suiteDims(TestStats.TST.CUBE, TestStats.IDX.PHC);
		suiteDims(TestStats.TST.CUBE, TestStats.IDX.KD_LEVY);
		suiteDims(TestStats.TST.CUBE, TestStats.IDX.KD_SAVA);
		suiteDims(TestStats.TST.CUBE, TestStats.IDX.CB);
		suiteDims(TestStats.TST.CUBE, TestStats.IDX.CBF);
	}
	
	private static void suiteVortex() {
		TestStats.TST tst = TestStats.TST.VORTEX;
		int DIM = 3;
		int DEPTH = 64;
		double p1 = 5.0;
//		test(1, tst, idx, 3, 64, 1*1000*1000, 1.0);
//		test(1, tst, idx, 4, 64, 1*1000*1000, 1.0);
//		test(1, tst, idx, 5, 64, 1*1000*1000, 1.0);
		testSeries(tst, TestStats.IDX.PHC, 	 DIM, DEPTH, p1, 1, 2, 5, 10, 15, 25, 50, 75, 100);//, 150, 200);//, 250);
		testSeries(tst, TestStats.IDX.KD_LEVY, DIM, DEPTH, p1, 1, 2, 5, 10, 15, 25, 50, 75, 100);//, 150, 200);
		testSeries(tst, TestStats.IDX.KD_SAVA, DIM, DEPTH, p1, 1, 2, 5, 10, 15, 25, 50, 75, 100);//, 150, 200);
		testSeries(tst, TestStats.IDX.CB, 	 DIM, DEPTH, p1, 1, 2, 5, 10, 15, 25, 50, 75, 100);//, 150, 200);
		testSeries(tst, TestStats.IDX.CBF, 	 DIM, DEPTH, p1, 1, 2, 5, 10, 15, 25, 50, 75, 100);//, 150, 200);
	}
		
	private static void suiteDimsSpace() {
		TestStats.IDX idx = TestStats.IDX.CBF;
		test(1, TestStats.TST.CUBE, idx, 3, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 4, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 5, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 6, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 8, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 10, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 15, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 20, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 30, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 40, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 50, 64, 1*1000*1000, 1.0);
		test(1, TestStats.TST.CUBE, idx, 60, 64, 1*1000*1000, 1.0);
	}
	
	private static void suiteDims(TestStats.TST test, TestStats.IDX idx) {
		suiteDims(test, idx, 10, 3.5);
	}
	
	private static void suiteDims(TestStats.TST test, TestStats.IDX idx, double nEntries, double p1) {
		int n = (int) (nEntries*1000*1000);
		int r = 3;
		test(r, test, idx, 2, 64, n, p1);
		test(r, test, idx, 3, 64, n, p1);
		test(r, test, idx, 4, 64, n, p1);
		test(r, test, idx, 5, 64, n, p1);
		test(r, test, idx, 6, 64, n, p1);
		if (idx == TestStats.IDX.RSS) {
			return;
		}
		test(r, test, idx, 8, 64, n, p1);
		test(r, test, idx, 10, 64, n, p1);
		test(r, test, idx, 12, 64, n, p1);
//		test(r, test, idx, 14, 64, n, p1);
		test(r, test, idx, 15, 64, n, p1);

		test(r, test, idx, 20, 64, n, p1);
//		test(r, test, idx, 25, 64, n, p1);
//		test(r, test, idx, 30, 64, n, p1);
//		test(r, test, idx, 40, 64, n, p1);
//		test(r, test, idx, 50, 64, n, p1);
//		test(r, test, idx, 60, 64, n, p1);
	}
	
	private static void suiteDimsR(TestStats.TST test, TestStats.IDX idx, double nEntries, double p1) {
		int n = (int) (nEntries*1000*1000);
		int r = 3;
		testR(r, test, idx, 2, 64, n, p1);
		testR(r, test, idx, 3, 64, n, p1);
		testR(r, test, idx, 4, 64, n, p1);
		if (idx == TestStats.IDX.RSS) {
			return;
		}
		testR(r, test, idx, 5, 64, n, p1);
		testR(r, test, idx, 6, 64, n, p1);
		testR(r, test, idx, 8, 64, n, p1);
		testR(r, test, idx, 10, 64, n, p1);
		testR(r, test, idx, 12, 64, n, p1);
//		testR(r, test, idx, 15, 64, n, p1);
//		testR(r, test, idx, 20, 64, n, p1);
	}
	
	private static void suiteSmoke() {
		
		//cube
		//warm-up
		test(2, TestStats.TST.CUBE, TestStats.IDX.PHC, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CUBE, IDX.PHC, 3, 64, 1*1000*1000, 1.0);
		test(2, TestStats.TST.CUBE, TestStats.IDX.KD_LEVY, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CUBE, IDX.KD_LEVY, 3, 64, 1*1000*1000, 1.0);
		test(2, TestStats.TST.CUBE, TestStats.IDX.KD_SAVA, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CUBE, IDX.KD_SAVA, 3, 64, 1*1000*1000, 1.0);
		test(2, TestStats.TST.CUBE, TestStats.IDX.CB, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CUBE, IDX.CB, 3, 64, 1*1000*1000, 1.0);
		test(2, TestStats.TST.CUBE, TestStats.IDX.CBF, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CUBE, IDX.CBF, 3, 64, 1*1000*1000, 1.0);

		//cluster
//		test(2, TST.CLUSTER, IDX.PHC, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CLUSTER, IDX.PHC, 3, 64, 1*1000*1000, 1.0);
//		test(2, TST.CLUSTER, IDX.KD_LEVY, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CLUSTER, IDX.KD_LEVY, 3, 64, 1*1000*1000, 1.0);
//		test(2, TST.CLUSTER, IDX.KD_SAVA, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CLUSTER, IDX.KD_SAVA, 3, 64, 1*1000*1000, 1.0);
//		test(2, TST.CLUSTER, IDX.CB, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CLUSTER, IDX.CB, 3, 64, 1*1000*1000, 1.0);
//		test(2, TST.CLUSTER, IDX.CBF, 3, 64, 1*1000*1000, 1.0);
//		test(3, TST.CLUSTER, IDX.CBF, 3, 64, 1*1000*1000, 1.0);

		//double check
		test(2, TestStats.TST.CUBE, TestStats.IDX.PHC, 3, 64, 1*1000*1000, 1.0);
		test(2, TestStats.TST.CLUSTER, TestStats.IDX.PHC, 3, 64, 1*1000*1000, 1.0);
	}

	private static void suite1() {
		int D=5;
		//cube
//		testSeries(TST.CUBE, IDX.PHC,     D, 64, 1.0, 1, 2, 5, 10, 15, 25, 50);//, 75, 100);//, 150, 200);//, 250);
//		testSeries(TST.CUBE, IDX.KD_LEVY, D, 64, 1.0, 1, 2, 5, 10, 15, 25, 50);//, 75, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.KD_SAVA, D, 64, 1.0, 1, 2, 5, 10, 15, 25, 50);//, 75, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.CB,      D, 64, 1.0, 1, 2, 5, 10, 15, 25, 50);//, 75, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.CBF,     D, 64, 1.0, 1, 2, 5, 10, 15, 25, 50);//, 75, 100);//, 150, 200);
		testSeries(TestStats.TST.CUBE, TestStats.IDX.KD_LEVY, D, 64, 1.0, 100);//, 75, 100);//, 150, 200);
		testSeries(TestStats.TST.CUBE, TestStats.IDX.CB,      D, 64, 1.0, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.KD_LEVY, D, 64, 1.0, 200);//, 75, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.KD_SAVA, D, 64, 1.0, 200);//, 75, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.CB,      D, 64, 1.0, 200);//, 150, 200);
//		testSeries(TST.CUBE, IDX.CBF,     D, 64, 1.0, 200);//, 150, 200);
//		test(1, TST.CUBE, IDX.PHC, D, 64, 75*1000*1000, 1.0);
//		testSeries(TST.CUBE, IDX.PHC,     D, 64, 1.0, 75, 100);//, 150, 200);//, 250);
//		testSeries(TST.CUBE, IDX.CB,      D, 64, 1.0, 75, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.CBF,     D, 64, 1.0, 75, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.KD_LEVY, D, 64, 1.0, 75);//, 150, 200);
//		testSeries(TST.CUBE, IDX.KD_SAVA, D, 64, 1.0, 75);//, 150, 200);
//		testSeries(TST.CUBE, IDX.KD_LEVY, D, 64, 1.0, 100);//, 150, 200);
//		testSeries(TST.CUBE, IDX.KD_SAVA, D, 64, 1.0, 100);//, 150, 200);
		
		//cluster
//		double p1 = 3.5;
//		testSeries(TST.CLUSTER, IDX.PHC, 	3, 64, p1, 1, 2, 5, 10, 15, 25, 50, 75, 100, 150, 200);//, 250);
//		testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 64, p1, 10, 15, 25);//, 50, 75, 100, 150);
//		//testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 64, p1, 1, 2, 5, 10, 15, 25);//, 50, 75, 100, 150);
//		testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 64, p1, 1, 2, 5, 10, 15, 25);//, 50, 75, 100, 150);
//		testSeries(TST.CLUSTER, IDX.CB, 	3, 64, p1, 1, 2, 5, 10, 15, 25);//, 50, 75, 100, 150);
//		testSeries(TST.CLUSTER, IDX.CBF, 	3, 64, p1, 1, 2, 5, 10, 15, 25);//, 50, 75, 100, 150);
//		testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 64, p1, 10, 15, 25);
//		testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 64, p1, 1, 5, 10, 15, 25, 50);
//		testSeries(TST.CLUSTER, IDX.CB, 	3, 64, p1, 50);
//		testSeries(TST.CLUSTER, IDX.CBF, 	3, 64, p1, 50);
//		p1 = 3.5;
//		testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 64, p1, 15);
//		testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 64, p1, 15);
//		testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 64, p1, 25);
//		testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 64, p1, 25);
//		testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 64, p1, 50);
//		testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 64, p1, 50);
//		p1 = 3.4;
//		testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 64, p1, 50);


//		testSeries(TST.VORTEX, IDX.PHC, 	3, 64, 5.0, 1, 2, 5, 10, 15, 25, 50, 75, 100, 150, 200);//, 250);
//		testSeries(TST.VORTEX, IDX.KD_LEVY, 3, 64, 5.0, 1, 2, 5, 10, 15, 25, 50, 75, 100, 150, 200);
//		testSeries(TST.VORTEX, IDX.KD_SAVA, 3, 64, 5.0, 1, 2, 5, 10, 15, 25, 50, 75, 100, 150, 200);
//		testSeries(TST.VORTEX, IDX.CB, 		3, 64, 5.0, 1, 2, 5, 10, 15, 25, 50, 75, 100, 150, 200);
//		testSeries(TST.VORTEX, IDX.CBF, 	3, 64, 5.0, 1, 2, 5, 10, 15, 25, 50, 75, 100, 150, 200);
	}

	private static void suiteRange() {
		//50 = max = ~14.2
		test(TestStats.TST.CUBE, TestStats.IDX.PHC, 2, 64, 10*1000, 0, true, 1.0);
		test(TestStats.TST.CUBE, TestStats.IDX.PRT, 2, 64, 10*1000, 0, true, 1.0);
	}
	
	private static void suiteTigerRange() {
		//50 = max = ~14.2
		test(TestStats.TST.TIGER, TestStats.IDX.PHC, 2, 64, 10*1000, 0, true, 1.0);
		test(TestStats.TST.TIGER, TestStats.IDX.PRT, 2, 64, 10*1000, 0, true, 1.0);
	}
	
	private static void suiteTigerPoint() {
		//50 = max = ~14.2
		testSeries(TestStats.TST.TIGER, TestStats.IDX.PHC, 2, 64, 0.0f, 1, 5, 10, 15, 20);
//		testSeries(TST.TIGER, IDX.CB, 2, 64, 0.0f, 1, 5, 10, 15, 20);
//		testSeries(TST.TIGER, IDX.CBF, 2, 64, 0.0f, 1, 5, 10, 15, 20);
//		testSeries(TST.TIGER, IDX.KD_LEVY, 2, 64, 0.0f, 1, 5, 10, 15, 20);
//		testSeries(TST.TIGER, IDX.KD_SAVA, 2, 64, 0.0f, 1, 5, 10, 15, 20);
	}
	
	private static void suitePHCC() {
		for (int d=1; d<=20; d++) {
			testSeries(TestStats.TST.CUBE, TestStats.IDX.PHCC, d, 64, 1.0, 1, 5, 10, 15, 20);
		}
	}
	
	
	// ===== Custom Tests =====
	
	public MainTestManager() {
		TestProcessLauncher.launchRmiRegistry();
	}
	
	public void reset() {
		stats.clear();
		avgStats.clear();
	}
	
	public void testSeries(TestStats.TST TEST, TestStats.IDX INDEX,
			int DIM, int DEPTH, double param1,
			Class<? extends TestPoint> testClass,
			Class<? extends Candidate> indexClass,
			double... Ns) {
		for (double N: Ns) {
			test(3, TEST, INDEX, DIM, DEPTH, (int)(N*1000*1000), param1, testClass, indexClass);
		}
	}
	
	public void test(int repeat, TestStats.TST TEST, TestStats.IDX INDEX,
			int DIM, int DEPTH, int N, double param1,
			Class<? extends TestPoint> testClass,
			Class<? extends Candidate> indexClass) { 
		for (int i = 0; i < repeat; i++) {
			test(TEST, INDEX, DIM, DEPTH, N, i, false, param1, testClass, indexClass);
		}
		if (repeat > 1) {
			avgStats.add(average(repeat));
			printSummary();
		}
	}
	
	public void test(TestStats.TST TEST, TestStats.IDX INDEX,
			int DIM, int DEPTH, int N, long SEED, 
			boolean isRangeData, double param1,
			Class<? extends TestPoint> testClass,
			Class<? extends Candidate> indexClass) {
		TestStats s0 = new TestStats(TEST, INDEX, N, DIM, DEPTH, isRangeData, param1);
		s0.setSeed(SEED);
		s0.testClass = testClass;
		s0.indexClass = indexClass;
		//We use two different TestStats instances here, because the 2nd is returned from RMI.
		TestStats s = TestManagerRMI.runTest(s0);
		stats.add(s);
		printSummary();
	}
	
	// ===== Standard Tests =====
	
	private static void testSeries(TestStats.TST TEST, TestStats.IDX INDEX, int DIM, int DEPTH, double param1, double... Ns) {
		for (double N: Ns) {
			test(3, TEST, INDEX, DIM, DEPTH, (int)(N*1000*1000), param1);
		}
	}

	private static void test(int repeat, TestStats.TST TEST, TestStats.IDX INDEX, int DIM, int DEPTH, int N, double param1) { 
		for (int i = 0; i < repeat; i++) {
			test(TEST, INDEX, DIM, DEPTH, N, i, false, param1);
		}
		if (repeat > 1) {
			avgStats.add(average(repeat));
			printSummary();
		}
	}

	private static void testSeriesWQS(TestStats.TST TEST, TestStats.IDX INDEX, int DIM, int DEPTH, double param1, double N, int...wqss) {
		for (int wqs: wqss) {
			TestStats.DEFAULT_W_QUERY_SIZE = wqs;
			test(3, TEST, INDEX, DIM, DEPTH, (int)(N*1000*1000), param1);
		}
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
	private static void testSeriesR(TestStats.TST TEST, TestStats.IDX INDEX, int DIM, int DEPTH, double param1, int... Ns) {
		for (int N: Ns) {
			testR(3, TEST, INDEX, DIM, DEPTH, N*1000*1000, param1);
		}
	}

	private static void testR(int repeat, TestStats.TST TEST, TestStats.IDX INDEX, int DIM, int DEPTH, int N, double param1) { 
		for (int i = 0; i < repeat; i++) {
			test(TEST, INDEX, DIM, DEPTH, N, i, true, param1);
		}
		if (repeat > 1) {
			avgStats.add(average(repeat));
			printSummary();
		}
	}

	private static void testSeriesWQSR(TestStats.TST TEST, TestStats.IDX INDEX, int DIM, int DEPTH, double param1, int N, int... wqss) {
		for (int wqs: wqss) {
			TestStats.DEFAULT_W_QUERY_SIZE = wqs;
			testR(3, TEST, INDEX, DIM, DEPTH, N*1000*1000, param1);
		}
	}

	
	private static void test(TestStats.TST TEST, TestStats.IDX INDEX, int DIM, int DEPTH, int N, long SEED, 
			boolean isRangeData, double param1) {
		TestStats s0 = new TestStats(TEST, INDEX, N, DIM, DEPTH, isRangeData, param1);
		s0.setSeed(SEED);
		//We use two different TestStats instances here, because the 2nd is returned from RMI.
		TestStats s = TestManagerRMI.runTest(s0);
		stats.add(s);
		printSummary();
	}


	public static void printSummary() {
		System.out.println("Summary-Avg:");
		System.out.println("============");
		for (TestStats s: avgStats) {
			System.out.println(s);
		}
		System.out.println("Summary:");
		System.out.println("========");
		for (TestStats s: stats) {
			System.out.println(s);
		}
	}

	private static TestStats average(int nStats) {
		int n = stats.size();
		List<TestStats> toAggregate = stats.subList(n-nStats-1, n-1); 
		TestStats avg = TestStats.aggregate(toAggregate);
		return avg;
	}
	

}
