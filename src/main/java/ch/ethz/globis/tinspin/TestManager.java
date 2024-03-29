/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.util.List;

import ch.ethz.globis.tinspin.TestInstances.IDX;
import ch.ethz.globis.tinspin.TestInstances.TST;
import ch.ethz.globis.tinspin.data.TestPoint;
import ch.ethz.globis.tinspin.util.Logging;
import ch.ethz.globis.tinspin.util.rmi.TestManagerRMI;
import ch.ethz.globis.tinspin.wrappers.Candidate;

public class TestManager {

	private static Logging log = new Logging();
	
	public static void main(String[] args) {
		long t1 = System.currentTimeMillis();
		try {
	
	        TestManagerRMI.PROCESS_OPTIONS = "-Xmx24G";

	        int[] sizesQuery = {1, 10, 100, 1000};//, 10000};
			double[] sizesData = new double[]{0.5, 1};//, 2.5, 5, 10, 25, 50};
			//double[] sizesData = new double[]{0.001, 0.005, 0.01, 0.05};

			sizesData = new double[]{0.1, 1};
//			testSeries(TST.CLUSTER_P, IDX.CBZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.RSZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.STRZ, 3, 5.0, sizesData);
			testSeries(TST.CLUSTER_P, IDX.PHC, 3, 5.0, sizesData);
			testSeries(TST.CLUSTER_P, IDX.PHC2, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.PHC_IPP, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.QTZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.QT2Z, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.KDZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.XTS, 3, 5.0, sizesData);
			
//			double p1;
//			p1 = 2.0; //25 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0, 5.0};//, 2.5, 5, 10, 25, 50};
//			testSeries(TST.HDF5, IDX.CTZ, 3, p1, sizesData);
//			p1 = 3.0; //128 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0};//, 2.5, 5, 10, 25, 50};
//			testSeries(TST.HDF5, IDX.CTZ, 3, p1, sizesData);
//			p1 = 1.0; //784 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1};
//			testSeries(TST.HDF5, IDX.CTZ, 3, p1, sizesData);

//			p1 = 1.0; //784 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1};
//			testSeries(TST.HDF5, IDX.PHC2, 3, p1, sizesData);

//			p1 = 4.0; //256 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5};//, 2.5, 5, 10, 25, 50};
//			testSeries(TST.HDF5, IDX.PHC2, 3, p1, sizesData);
//
//			p1 = 5.0; //50 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5};//, 2.5, 5, 10, 25, 50};
//			testSeries(TST.HDF5, IDX.PHC2, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.PHC_IPP, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.RSZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.KDZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.CTZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.FCT, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.WCT, 3, p1, sizesData);
//
//			if (true) {
//				return;
//			}
			
			
//			p1 = 2.0; //25 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0, 5.0};//, 2.5, 5, 10, 25, 50};
//			testSeries(TST.HDF5, IDX.PHC2, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.PHC_IPP, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.RSZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.KDZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.CTZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.FCT, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.WCT, 3, p1, sizesData);
//			
//			p1 = 3.0; //128 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1, 0.5, 1.0};//, 2.5, 5, 10, 25, 50};
//			testSeries(TST.HDF5, IDX.PHC2, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.PHC_IPP, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.RSZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.KDZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.CTZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.FCT, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.WCT, 3, p1, sizesData);
//
//			p1 = 1.0; //784 dim
//			sizesData = new double[]{0.001, 0.005, 0.01, 0.05, 0.1};
//			//testSeries(TST.HDF5, IDX.PHC2, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.PHC_IPP, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.RSZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.KDZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.CTZ, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.FCT, 3, p1, sizesData);
//			testSeries(TST.HDF5, IDX.WCT, 3, p1, sizesData);
//
//			if (true) {
//				return;
//			}
			
//			testSeriesWQS(TST.CLUSTER, IDX.RSZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.QT0Z, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.QT2Z, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER_P, IDX.PHC2, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.KDZ, 3, 5.0, 1, sizesQuery);

			sizesData = new double[]{0.1, 0.5, 1, 2.5, 5, 10, 25, 50};
//			testSeries(TST.CLUSTER_P, IDX.CBZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.RSZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.STRZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.PHC, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.PHC2, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.PHC_IPP, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.QTZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.QT2Z, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.KDZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER_P, IDX.XTS, 3, 5.0, sizesData);
//			
//			suiteDims(TST.CLUSTER, IDX.RSZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.STRZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.PHC2, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.PHC_IPP, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.QT0Z, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.QT2Z, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.KDZ, 1, 5.0);

			
//			testSeriesWQS(TST.CLUSTER, IDX.PHC, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.PHC2, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.RSZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.QT0Z, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.QT2Z, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.PHC, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.PHC2, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.PHC_IPP, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC2, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC_IPP, 1, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.PHC, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.PHC2, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.QT0Z, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.QT2Z, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.RSZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.STRZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.PHC, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.PHC2, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.PHC_IPP, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.PHC, 2, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.PHC2, 2, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.PHC_IPP, 2, 1.0, 1, sizesQuery);

//			if (true) return;
			
//			testP(TST.CUBE, IDX.RSS, 30, 1000000, 1.0);
//			testP(TST.CUBE, IDX.RSS, 40, 1000000, 1.0);
//			log.writeLogFileForTestSeries("dimsP");

			
//			suiteDims(TST.CUBE, IDX.PHC_IPP, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.QT0Z, 1, 1.0);
//			suiteDims(TST.CLUSTER, IDX.QT0Z, 1, 5.0);

//			testSeriesWQS(TST.CLUSTER, IDX.PHC, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.PHC, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.PHC_IPP, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC_IPP, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.PHC2, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.PHC2, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC2, 2, 1.0, 1, sizesQuery);
			
//			testSeries(TST.CLUSTER, IDX.PHC, 3, 5.0, sizesData);
//			testSeries(TST.CUBE, IDX.PHC, 3, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.PHC, 2, 1.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, sizesData);
//			testSeries(TST.CUBE, IDX.PHC_IPP, 3, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.PHC_IPP, 2, 1.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC2, 3, 5.0, sizesData);
//			testSeries(TST.CUBE, IDX.PHC2, 3, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.PHC2, 2, 1.0, sizesData);
			
//			suiteDims(TST.CLUSTER, IDX.PHC, 1, 5.0);
//			suiteDims(TST.CUBE, IDX.PHC, 1, 1.0);
//			suiteDims(TST.CLUSTER, IDX.PHC_IPP, 1, 5.0);
//			suiteDims(TST.CUBE, IDX.PHC_IPP, 1, 1.0);
//			suiteDims(TST.CLUSTER, IDX.PHC2, 1, 5.0);
//			suiteDims(TST.CUBE, IDX.PHC2, 1, 1.0);
//			suiteDims(TST.CLUSTER, IDX.RSZ, 1, 5.0);
//			suiteDims(TST.CUBE, IDX.RSZ, 1, 1.0);
//			
//			testSeries(TST.CLUSTER, IDX.KDZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 5.0, sizesData);
//			suiteDims(TST.CLUSTER, IDX.KDZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.KD_SAVA, 1, 5.0);

			
			
//			sizesData = new double[]{0.5, 1, 2.5, 5, 10, 25, 50};
//			testSeriesR(TST.OSM, IDX.PHC2, 2, 1.0, sizesData);
//			testSeriesR(TST.OSM, IDX.PHC_IPP, 2, 1.0, sizesData);
//			testSeriesR(TST.OSM, IDX.RSS, 2, 1.0, sizesData);
//			testSeriesR(TST.OSM, IDX.RSZ, 2, 1.0, sizesData);
//			testSeriesR(TST.OSM, IDX.STRZ, 2, 1.0, sizesData);
//			testSeriesR(TST.OSM, IDX.QTZ, 2, 1.0, sizesData);
//			testSeriesR(TST.OSM, IDX.QT0Z, 2, 1.0, sizesData);
//			testSeriesR(TST.OSM, IDX.XTS, 2, 1.0, sizesData);

//			sizesData = new double[]{0.5, 1, 2.5, 5, 10, 25, 50};
//			testSeries(TST.CUBE, IDX.CBF, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.CBZ, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.PHC2, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.PHC_IPP, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.RSS, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.RSZ, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.STRZ, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.KDZ, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.KD_LEVY, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.KD_SAVA, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.QTZ, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.QT2Z, 3, 1.0, sizesData);
//			testSeries(TST.CUBE, IDX.XTS, 3, 1.0, sizesData);
//
//			sizesData = new double[]{0.5, 1, 2.5, 5, 10, 25, 50};
//			testSeries(TST.OSM, IDX.CBF, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.CBZ, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.PHC2, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.PHC_IPP, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.RSS, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.RSZ, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.STRZ, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.KDZ, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.KD_LEVY, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.KD_SAVA, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.QTZ, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.QT2Z, 2, 1.0, sizesData);
//			testSeries(TST.OSM, IDX.XTS, 2, 1.0, sizesData);
//
//			sizesData = new double[]{0.5, 1, 2.5, 5, 10, 25};
//			testSeries(TST.TIGER, IDX.CBF, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.CBZ, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.PHC, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.PHC_IPP, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.RSS, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.RSZ, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.STRZ, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.KD_LEVY, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.KD_SAVA, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.QTZ, 2, 1.0, sizesData);
//			testSeries(TST.TIGER, IDX.XTS, 2, 1.0, sizesData);
//
//			sizesData = new double[]{0.5, 1, 2.5, 5, 10, 25, 50};
//			testSeries(TST.CLUSTER, IDX.CBF, 3, 3.4, sizesData);
//OLD			testSeries(TST.CLUSTER, IDX.CBZ, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC_IPP, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSS, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSZ, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.STRZ, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.QTZ, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.QT0Z, 3, 3.4, sizesData);
//			testSeries(TST.CLUSTER, IDX.XTS, 3, 3.4, sizesData);
//
//			testSeries(TST.CLUSTER, IDX.CBF, 3, 3.5, sizesData);
//OLD			testSeries(TST.CLUSTER, IDX.CBZ, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC_IPP, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSS, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSZ, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.STRZ, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.QTZ, 3, 3.5, sizesData);
//			testSeries(TST.CLUSTER, IDX.XTS, 3, 3.5, sizesData);

//			testSeries(TST.CLUSTER, IDX.CBF, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.CBZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSS, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.RSZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.STRZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_LEVY, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.KD_SAVA, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.QTZ, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.QT2Z, 3, 5.0, sizesData);
//			testSeries(TST.CLUSTER, IDX.XTS, 3, 5.0, sizesData);
			
//			suiteDims(TST.CLUSTER, IDX.CBF, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.CBZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.PHC, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.PHC_IPP, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.RSS, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.RSZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.STRZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.KD_LEVY, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.KD_SAVA, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.KDZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.QTZ, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.QT2Z, 1, 5.0);
//			suiteDims(TST.CLUSTER, IDX.XTS, 1, 5.0);

//			suiteDims(TST.CUBE, IDX.CBF, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.CBZ, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.PHC2, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.PHC_IPP, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.RSS, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.RSZ, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.STRZ, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.KD_LEVY, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.KD_SAVA, 1, 1.0);
//			suiteDims(TST.CUBE, IDX.KDZ, 1, 1.0);
//			suiteDims(TST.CUBE_P, IDX.QTZ, 1, 1.0);
//			suiteDims(TST.CUBE_P, IDX.QT2Z, 1, 1.0);
//			suiteDims(TST.CUBE_P, IDX.XTS, 1, 1.0);
			
//			suiteDims(TST.CLUSTER, IDX.CBF, 1, 3.4);
//OLD			suiteDims(TST.CLUSTER, IDX.CBZ, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.PHC_IPP, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.RSS, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.RSZ, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.STRZ, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.KD_LEVY, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.KD_SAVA, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.QTZ, 1, 3.4);
//			suiteDims(TST.CLUSTER, IDX.XTS, 1, 3.4);
//			
//			suiteDims(TST.CLUSTER, IDX.CBF, 1, 3.5);
//OLD			suiteDims(TST.CLUSTER, IDX.CBZ, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.PHC, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.PHC_IPP, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.RSS, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.RSZ, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.STRZ, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.KD_LEVY, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.KD_SAVA, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.QTZ, 1, 3.5);
//			suiteDims(TST.CLUSTER, IDX.XTS, 1, 3.5);

			
//			sizesQuery = new int[]{1, 10, 100, 1000, 10000};
//			testSeriesWQS(TST.CUBE_P, IDX.CBZ, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.KD_LEVY, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.KD_SAVA, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.KDZ, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.PHC2, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.PHC_IPP, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.QT2Z, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.QTZ, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE, IDX.RSS, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.RSZ, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.STRZ, 3, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.CUBE_P, IDX.XTS, 3, 1.0, 1, sizesQuery);
//
//			suiteDims(TST.CUBE_P, IDX.QT2Z, 1, 1.0);
//			suiteDims(TST.CUBE_P, IDX.XTS, 1, 1.0);

//			testSeriesWQS(TST.OSM, IDX.CBZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.KD_LEVY, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.KD_SAVA, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.PHC_IPP, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.QT0Z, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.QTZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.RSS, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.RSZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.STRZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.OSM, IDX.XTS, 2, 1.0, 1, sizesQuery);

//			testSeriesWQS(TST.TIGER, IDX.CBZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.KD_LEVY, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.KD_SAVA, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.PHC, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.PHC_IPP, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.QTZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.RSS, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.RSZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.STRZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQS(TST.TIGER, IDX.XTS, 2, 1.0, 1, sizesQuery);

//			testSeriesWQS(TST.CLUSTER, IDX.CBZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.KD_LEVY, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.KD_SAVA, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.PHC, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.QT0Z, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.QTZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.RSS, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.RSZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.STRZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQS(TST.CLUSTER, IDX.XTS, 3, 5.0, 1, sizesQuery);
//
//			testSeriesWQSR(TST.CUBE, IDX.PHC, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.PHC_IPP, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.QTZ, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.RSS, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.RSZ, 3, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CUBE, IDX.STRZ, 3, 1.0, 1, sizesQuery);
//		
//			testSeriesWQSR(TST.OSM, IDX.PHC, 2, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.PHC_IPP, 2,1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.QTZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.RSS, 2, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.RSZ, 2, 1.0, 1, sizesQuery);
//			testSeriesWQSR(TST.OSM, IDX.STRZ, 2, 1.0, 1, sizesQuery);
//		
//			testSeriesWQSR(TST.CLUSTER, IDX.PHC, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.PHC_IPP, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.QTZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.RSS, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.RSZ, 3, 5.0, 1, sizesQuery);
//			testSeriesWQSR(TST.CLUSTER, IDX.STRZ, 3, 5.0, 1, sizesQuery);

//			sizesData = new double[]{0.5, 1, 2.5, 5, 10, 25, 50};
//			testSeriesR(TST.CUBE_R, IDX.PHC2, 3, 1.0, sizesData);
//			testSeriesR(TST.CUBE_R, IDX.PHC_IPP, 3, 1.0, sizesData);
//			testSeriesR(TST.CUBE, IDX.RSS, 3, 1.0, sizesData);
//			testSeriesR(TST.CUBE_R, IDX.RSZ, 3, 1.0, sizesData);
//			testSeriesR(TST.CUBE_R, IDX.STRZ, 3, 1.0, sizesData);
//			testSeriesR(TST.CUBE_R, IDX.QTZ, 3, 1.0, sizesData);

//			suiteDimsR(TST.CUBE_R, IDX.PHC2, 1, 1.0);
//			suiteDimsR(TST.CUBE_R, IDX.PHC_IPP, 1, 1.0);
//			suiteDimsR(TST.CUBE, IDX.RSS, 1, 1.0);
//			suiteDimsR(TST.CUBE_R, IDX.RSZ, 1, 1.0);
//			suiteDimsR(TST.CUBE_R, IDX.STRZ, 1, 1.0);
//			suiteDimsR(TST.CUBE_R, IDX.QTZ, 1, 1.0);

//			testSeriesR(TST.CLUSTER_R, IDX.PHC2, 3, 5.0, sizesData);
//			testSeriesR(TST.CLUSTER_R, IDX.PHC_IPP, 3, 5.0, sizesData);
//			testSeriesR(TST.CLUSTER, IDX.RSS, 3, 5.0, sizesData);
//			testSeriesR(TST.CLUSTER_R, IDX.RSZ, 3, 5.0, sizesData);
//			testSeriesR(TST.CLUSTER_R, IDX.STRZ, 3, 5.0, sizesData);
//			testSeriesR(TST.CLUSTER_R, IDX.QTZ, 3, 5.0, sizesData);

//			suiteDimsR(TST.CLUSTER, IDX.PHC, 1, 5.0);
//			suiteDimsR(TST.CLUSTER, IDX.PHC_IPP, 1, 5.0);
//			suiteDimsR(TST.CLUSTER, IDX.RSS, 1, 5.0);
//			suiteDimsR(TST.CLUSTER, IDX.RSZ, 1, 5.0);
//			suiteDimsR(TST.CLUSTER, IDX.STRZ, 1, 5.0);
//			suiteDimsR(TST.CLUSTER, IDX.QTZ, 1, 5.0);

		} finally {
			log.printSummary();
			long t2 = System.currentTimeMillis();
			long t = t2-t1;
			System.out.println("Total time: " + t + "[ms] = " + t/1000 + "[s] = " + t/60000 + "[m]");
		}
	}
	
	private static void suiteDims(TST test, IDX idx, double nEntries, double p1) {
		int n = (int) (nEntries*1000*1000);
		testP(test, idx, 2, n, p1);
		testP(test, idx, 3, n, p1);
		testP(test, idx, 4, n, p1);
		testP(test, idx, 5, n, p1);
		testP(test, idx, 6, n, p1);
		testP(test, idx, 8, n, p1);
		testP(test, idx, 10, n, p1);
		testP(test, idx, 12, n, p1);
		testP(test, idx, 16, n, p1);
		testP(test, idx, 20, n, p1);

		switch (idx) {
		case CBF:
		case CBZ:
		case QTZ:
		case QT0Z:
		case QT2Z:
		default:
			testP(test, idx, 24, n, p1);
//			testP(test, idx, 28, n, p1);
			testP(test, idx, 30, n, p1);
//			testP(test, idx, 40, n, p1);
//			testP(test, idx, 50, n, p1);

//			testP(test, idx, 60, n, p1);
//			testP(test, idx, 80, n, p1);
//			testP(test, idx, 100, n, p1);
//			testP(test, idx, 150, n, p1);
//			testP(test, idx, 200, n, p1);
		}


		
		
		log.writeLogFileForTestSeries("dimsP");
	}
	
	private static void suiteDimsR(TST test, IDX idx, double nEntries, double p1) {
		int n = (int) (nEntries*1000*1000);
		testR(test, idx, 2, n, p1);
		testR(test, idx, 3, n, p1);
		testR(test, idx, 4, n, p1);
		testR(test, idx, 5, n, p1);
		testR(test, idx, 6, n, p1);
		testR(test, idx, 8, n, p1);
		testR(test, idx, 10, n, p1);
		testR(test, idx, 12, n, p1);
		testR(test, idx, 16, n, p1);
		testR(test, idx, 20, n, p1);
		
		switch (idx) {
		case CBF:
		case CBZ:
		case QTZ:
			break;
		default:
			testR(test, idx, 24, n, p1);
			testR(test, idx, 28, n, p1);
		}
		
		log.writeLogFileForTestSeries("dimsR");
	}
		
	
	// ===== Custom Tests =====
	
	public TestManager() {
		//
	}
	
	public void test(TestStats ts) {
		repeatTest(ts);
//		int repeat = ts.cfgNRepeat;
//		for (int i = 0; i < repeat; i++) {
//			//We use two different TestStats instances here, because the 2nd is returned from RMI.
//			TestStats s = TestManagerRMI.runTest(ts);
//			log.logResult(s);
//			log.printSummary();
//		}
//		if (repeat > 1) {
//			log.logResultAverage(repeat);
//			log.printSummary();
//		}
	}
	
	public void test(int repeat, TestHandle data, IndexHandle index,
			int dims, int N, double param1,
			Class<? extends TestPoint> testClass,
			Class<? extends Candidate> indexClass) { 
		for (int i = 0; i < repeat; i++) {
			test(data, index, dims, N, i, param1, testClass, indexClass);
		}
		if (repeat > 1) {
			log.logResultAverage(repeat);
			log.printSummary();
		}
	}
	
	public void test(TestHandle data, IndexHandle index,
			int dims, int N, long SEED, 
			double param1,
			Class<? extends TestPoint> testClass,
			Class<? extends Candidate> indexClass) {
		TestStats s0 = new TestStats(data, index, N, dims, param1);
		s0.setSeed(SEED);
		s0.testClass = testClass;
		s0.indexClass = indexClass;
		//We use two different TestStats instances here, because the 2nd is returned from RMI.
		TestStats s = TestManagerRMI.runTest(s0);
		log.logResult(s);
		log.printSummary();
	}
	
	// ===== Standard Tests =====
	
	private static void testSeries(TST data, IDX index, int dims, double param1, double... Ns) {
		for (double N: Ns) {
			testP(data, index, dims, (int)(N*1000*1000), param1);
		}
		log.writeLogFileForTestSeries("sizeP");
	}

	private static void testP(TST data, IDX index, int dims, int N, double param1) { 
		TestStats cfg = createTest(data, index, dims, N, param1);
		repeatTest(cfg);
	}

	/**
	 * Test varying result size for window queries.
	 * @param data
	 * @param index
	 * @param dims
	 * @param param1
	 * @param N
	 * @param wqss
	 */
	private static void testSeriesWQS(TST data, IDX index, int dims, 
			double param1, double N, int...wqss) {
		for (int wqs: wqss) {
			TestStats cfg = new TestStats(data, index, (int)(N*1000*1000), dims, param1);
			cfg.cfgWindowQuerySize = wqs;
			cfg.cfgWindowQueryRepeat = 10000;
			repeatTest(cfg);
		}
		log.writeLogFileForTestSeries("sizePWQS");
	}


	private static void testSeriesR(TST data, IDX index, int dims, 
			double param1, double... Ns) {
		for (double N: Ns) {
			testR(data, index, dims, (int) (N*1000*1000), param1);
		}
		log.writeLogFileForTestSeries("sizeR");
	}

	
	private static void testR(TST data, IDX index, int dims, int N, double param1) { 
		TestStats cfg = createTest(data, index, dims, N, param1);
		repeatTest(cfg);
	}

	/**
	 * Test varying result size for window queries.
	 * @param data
	 * @param index
	 * @param dims
	 * @param param1
	 * @param N
	 * @param wqss
	 */
	private static void testSeriesWQSR(TST data, IDX index, int dims, 
			double param1, int N, int... wqss) {
		for (int wqs: wqss) {
			TestStats cfg = createTest(data, index, dims, 
					N*1000*1000, param1);
			cfg.cfgWindowQuerySize = wqs;
			cfg.cfgWindowQueryRepeat = 10000;
			repeatTest(cfg);
		}
		log.writeLogFileForTestSeries("sizeRWQS");
	}

	
	private static void runOneTest(TestStats cfg, long seed) {
		//We use two different TestStats instances here, because the 2nd is returned from RMI.
		cfg.setSeed(seed);
		TestStats s = TestManagerRMI.runTest(cfg);
		log.logResult(s);
		log.printSummary();
	}

	private static void repeatTest(TestStats cfg) { 
		for (int i = 0; i < cfg.cfgNRepeat; i++) {
			runOneTest(cfg, i);
		}
		if (cfg.cfgNRepeat > 1) {
			log.logResultAverage(cfg.cfgNRepeat);
			log.printSummary();
		}
	}

	
	private static TestStats createTest(TST data, IDX index, int dims, 
			int N, double param1) {
		return new TestStats(data, index, N, dims, param1);
	}
	
	/**
	 * Run the provided test remotely in a separate process.
	 * The averaged results are returned in a separate instance.
	 * @param cfg test configuration
	 * @return results
	 */
	public static TestStats runTest(TestStats cfg) {
		for (int i = 0; i < cfg.cfgNRepeat; i++) {
			runOneTest(cfg, i);
		}
		TestStats avg = log.logResultAverage(cfg.cfgNRepeat);
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
