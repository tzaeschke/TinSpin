/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.data.TestPoint;
import ch.ethz.globis.tinspin.wrappers.Candidate;
import ch.ethz.globis.tinspin.wrappers.PointCritBitZ;
import ch.ethz.globis.tinspin.wrappers.PointPHC;
import ch.ethz.globis.tinspin.wrappers.PointPHC2;
import ch.ethz.globis.tinspin.wrappers.PointPHC2_IPP;
import ch.ethz.globis.tinspin.wrappers.PointPHCF;
import ch.ethz.globis.tinspin.wrappers.PointPHC_IPP;
import ch.ethz.globis.tinspin.wrappers.PointQuadZ;
import ch.ethz.globis.tinspin.wrappers.RectanglePHC;
import ch.ethz.globis.tinspin.wrappers.RectanglePHC2;
import ch.ethz.globis.tinspin.wrappers.RectanglePHCF;
import ch.ethz.globis.tinspin.wrappers.RectanglePHC_IPP;
import ch.ethz.globis.tinspin.wrappers.RectangleQuadZ;

public class TestStats implements Serializable, Cloneable {

	/**
	 * Enum with shortcuts to the candidate test classes.
	 * 
	 * The class names can be overridden in the TestStats class.
	 */
	public enum IDX {
		//Our implementations
		//===================
		PHC(PointPHC.class.getName(), RectanglePHC.class.getName()),
		PHC2(PointPHC2.class.getName(), RectanglePHC2.class.getName()),
		/** based on PhTreeF */
		PHCF(PointPHCF.class.getName(), RectanglePHCF.class.getName()), 
		/** CUBE with Integer pre-processor. */
		PHC_IPP(PointPHC_IPP.class.getName(), RectanglePHC_IPP.class.getName()),
		PHC2_IPP(PointPHC2_IPP.class.getName(), null),
		/** CritBit by Tilmann */
		CBZ(PointCritBitZ.class.getName(), ""),
		/** Quadtree by Tilmann */
		QKDZ(PointQuadZ.class.getName(), RectangleQuadZ.class.getName()),

		//3rd party implementations
		//=========================
		PRT("ch.ethz.globis.tinspin.wrappers.PointPRT", 
				"ch.ethz.globis.tinspin.wrappers.RectanglePRT"),
		/** R*Tree by lokeshj */
		RSL("ch.ethz.globis.tinspin.wrappers.PointRSLokeshj", 
				"ch.ethz.globis.tinspin.wrappers.Rectangle.RSLokeshj"),
		/** R*Tree by Seeger */
		RSS("ch.ethz.globis.tinspin.wrappers.PointRStarSeeger", 
				"ch.ethz.globis.tinspin.wrappers.RectangleRStarSeeger"),
		CBR("ch.ethz.globis.tinspin.wrappers.PointCBR", null),
		CBF("ch.ethz.globis.tinspin.wrappers.PointCBF", null),
		XTR("ch.ethz.globis.tinspin.wrappers.PointXtree", 
				"ch.ethz.globis.tinspin.wrappers.RectangleXtree"),
		KD_LEVY("ch.ethz.globis.tinspin.wrappers.PointKDL", null),
		KD_SAVA("ch.ethz.globis.tinspin.wrappers.PointKDS", null),
		KD_RED("ch.ethz.globis.tinspin.wrappers.PointKDRed", null),
		
		//Experimental implementations
		//============================
		CUSTOM("", ""), 
		HIL("", ""), 
		MX_CIF("", ""),
		OCT("", ""),
		/** original PH tree */
		PHCv1("ch.ethz.globis.tinspin.wrappers.PointPHC_v1", ""),
		/** based on PhEntry */
		PHC_PHE("ch.ethz.globis.tinspin.wrappers.PointPHC_PHE", null),
		/** C++ version of PH-Tree */
		PHCC("ch.ethz.globis.tinspin.wrappers.PointPHCCTree", null),
		/** Uses a region tree for points. */
		PHC_RECTANGLE("ch.ethz.globis.tinspin.wrappers.PointPHCRectangle", null),
		
		//Other
		//=====
		DUMMY("", ""),
		CUSTOM1(null, null),
		CUSTOM2(null, null),
		CUSTOM3(null, null),
		USE_PARAM_CLASS(null, null);

		private final String candidateClassNamePoint;
		private final String candidateClassNameRectangle;

		IDX(String candidateClassNamePoint, 
				String candidateClassNameRectangle) {
			this.candidateClassNamePoint = candidateClassNamePoint;
			this.candidateClassNameRectangle = candidateClassNameRectangle;
		}

		public String getCandidateClassNamePoint() {
			return candidateClassNamePoint;
		}

		public String getCandidateClassNameRectangle() {
			return candidateClassNameRectangle;
		}
	}

	public enum TST {
		CUBE,
		CLUSTER,
		SKYLINE,
		TIGER, 
		TIGER32,
		OSM,
		MBR_SIZE,
		MBR_ASPECT, 
		VORTEX,
		CUSTOM,
		TOUCH,
		CSV;
	}

	public static int DEFAULT_W_QUERY_SIZE = 1000;
	public static int DEFAULT_N_WINDOW_QUERY = 1000; //number of range queries
	public static int DEFAULT_N_POINT_QUERY = 1000*1000; //number of point queries
	public static int DEFAULT_N_KNN_QUERY = 10*100;
	public static int DEFAULT_N_UPDATES = 100*1000;
	public static int DEFAULT_N_UPDATE_CYCLES = 10;


	/** */
	private static final long serialVersionUID = 1L;
	public TestStats(TestStats.TST test, TestStats.IDX index, int N, int DIM, boolean isRangeData,
			double param1) {
		this(test, index, N, DIM, isRangeData, param1, 0);
	}
	public TestStats(TestStats.TST test, TestStats.IDX index, int N, int DIM, boolean isRangeData,
			double param1, double param2) {
		this.cfgNEntries = N;
		this.cfgNDims = DIM;
		this.INDEX = index;
		this.TEST = test;
		this.SEEDmsg = "" + seed;
		this.isRangeData = isRangeData;
		this.param1 = param1;
		this.param2 = param2;
	}

	//configuration
	/** how often to repeat the test. */
	int cfgNRepeat = 3;
	int cfgNBits = 64; //default
	public int cfgNDims;
	public int cfgNEntries;

	/** How often kNN queries are repeated. This is reduced
	 * automatically with increasing dimensionality. */
	public int cfgKnnQueryBaseRepeat = DEFAULT_N_KNN_QUERY;
	public int cfgPointQueryRepeat = DEFAULT_N_POINT_QUERY;
	public int cfgUpdateRepeat = DEFAULT_N_UPDATE_CYCLES;
	public int cfgUpdateSize = DEFAULT_N_UPDATES;
	public int cfgWindowQueryRepeat = DEFAULT_N_WINDOW_QUERY;
	public int cfgWindowQuerySize = DEFAULT_W_QUERY_SIZE;
	
	public final TestStats.IDX INDEX;
	public final TestStats.TST TEST;
	public String SEEDmsg;
	public long seed;
	public final double param1;
	public double param2;
	public String paramStr;
	public boolean paramUseGC = true;
	public final boolean isRangeData;

	Class<? extends TestPoint> testClass;
	Class<? extends Candidate> indexClass;

	//results
	long statTGen;
	long statTLoad;
	long statTUnload;
	long statTq1;
	long statTq1E;
	long statTq2;
	long statTq2E;
	long statTqp1;
	long statTqp1E;
	long statTqp2;
	long statTqp2E;
	long statTqk1_1;
	long statTqk1_1E;
	long statTqk1_2;
	long statTqk1_2E;
	long statTqk10_1;
	long statTqk10_1E;
	long statTqk10_2;
	long statTqk10_2E;
	long statTu1;
	long statTu1E;
	long statTu2;
	long statTu2E;
	public int statNnodes;
	public long statNpostlen;
	public int statNNodeAHC;
	int statNNodeNT;
	int statNNodeInternalNT;
	int statNq1;
	int statNq2;
	int statNqp1;
	int statNqp2;
	double statDqk1_1;
	double statDqk1_2;
	double statDqk10_1;
	double statDqk10_2;
	int statNu1;
	int statNu2;
	long statSMin;
	long statSCalc;
	public long statSjvmF;
	public long statSjvmE;
	long statGcDiffL;
	long statGcTimeL;
	long statGcDiffWq;
	long statGcTimeWq;
	long statGcDiffPq;
	long statGcTimePq;
	long statGcDiffUp;
	long statGcTimeUp;
	long statGcDiffK1;
	long statGcTimeK1;
	long statGcDiffK10;
	long statGcTimeK10;
	long statGcDiffUl;
	long statGcTimeUl;
	String assortedInfo = "";

	Throwable exception = null;

	public void setFailed(Throwable t) {
		SEEDmsg = SEEDmsg + "-F";
		exception = t;
	}

	public void setSeed(long seed) {
		this.seed = seed;
		SEEDmsg = Long.toString(seed);
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	public TestStats cloneStats() {
		try {
			return (TestStats) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		String D = "\t"; //delimiter
		String ret = "";

		ret += INDEX.name();

		ret += "-" + (isRangeData ? "R" : "P");
		ret += "-" + SEEDmsg + D;

		ret += TEST.name() + "(" + param1 + ", " + param2 + ", " + paramStr + ")" + D;
		ret += cfgNDims + D + cfgNBits + D + cfgNEntries + D; 
		ret += (statSMin>>20) + D + (statSCalc>>20) + D + statSjvmF + D + statSjvmE + D; 
		ret += statTGen + D + statTLoad + D;
		//			ret += statTq1 + D + statTq1E + D + statTq2 + D + statTq2E + D + statTqp1 + D + statTqp1E + D + statTqp2 + D + statTqp2E + D;
		ret += statTq1E + D + statTq2E + D + statTqp1E + D + statTqp2E + D; 
		ret += statTu1E + D + statTu2E + D;
		ret += statTqk1_1E + D + statTqk1_2E + D;
		ret += statTqk10_1E + D + statTqk10_2E + D;
		ret += statNnodes + D + statNpostlen + D + statNNodeAHC + D + statNNodeNT + D + statNNodeInternalNT + D;
		ret += statNq1 + D + statNq2 + D + statNqp1 + D + statNqp2 + D;
		ret += statNu1 + D + statNu2 + D;
		ret += statDqk1_1 + D + statDqk1_2 + D + statDqk10_1 + D + statDqk10_2 + D;
		ret += statTUnload + D;
		ret += (statTLoad*1000000/cfgNEntries) + D + (statTUnload*1000000/cfgNEntries) + D;
		ret += statGcDiffL/1000000 + D + statGcTimeL + D;
		ret += statGcDiffWq/1000000 + D + statGcTimeWq + D;
		ret += statGcDiffPq/1000000 + D + statGcTimePq + D;
		ret += statGcDiffUp/1000000 + D + statGcTimeUp + D;
		ret += statGcDiffK1/1000000 + D + statGcTimeK1 + D;
		ret += statGcDiffK10/1000000 + D + statGcTimeK10 + D;
		ret += statGcDiffUl/1000000 + D + statGcTimeUl + D;
		ret += assortedInfo;
		if (exception != null) {
			ret += D + exception.getMessage();
		}
		return ret;
	}
	public void setN(int N) {
		cfgNEntries = N;
	}
	public int getN() {
		return cfgNEntries;
	}
	public void setStats(PhTreeStats q) {
		statNnodes = q.getNodeCount();
		statSCalc = q.getCalculatedMemSize();
		statNpostlen = (long) q.getAvgPostlen(null);
		statNNodeAHC = q.getAhcCount();
		statNNodeNT = q.getNtCount();
		statNNodeInternalNT = q.getNtInternalNodeCount();
		cfgNBits = q.getBitDepth();
	}
	public static TestStats aggregate(List<TestStats> stats) {
		TestStats t1 = stats.get(0);
		//TestStats avg = new TestStats(t1.TEST, t1.INDEX, t1.cfgNEntries, t1.cfgNDims, 
		//		t1.isRangeData, t1.param1);
		TestStats avg = t1.cloneStats();
//		avg.cfgNBits = t1.cfgNBits;
//		avg.cfgNEntries = t1.cfgNEntries;
//		avg.param2 = t1.param2;
//		avg.paramStr = t1.paramStr;
//		avg.paramWQSize = t1.paramWQSize;

		int cnt = 0;
		for (int i = 0; i < stats.size(); i++) {
			TestStats t = stats.get(i);

//			avg.testClass = t.testClass;
//			avg.indexClass = t.indexClass;

			if (t.exception != null) {
				//skip failed results
				continue;
			}
			avg.statTGen += t.statTGen;
			avg.statTLoad += t.statTLoad;
			avg.statTUnload += t.statTUnload;
			avg.statTq1 += t.statTq1;
			avg.statTq1E += t.statTq1E;
			avg.statTq2 += t.statTq2;
			avg.statTq2E += t.statTq2E;
			avg.statTqp1 += t.statTqp1;
			avg.statTqp1E += t.statTqp1E;
			avg.statTqp2 += t.statTqp2;
			avg.statTqp2E += t.statTqp2E;
			avg.statTqk1_1 += t.statTqk1_1;
			avg.statTqk1_1E += t.statTqk1_1E;
			avg.statTqk1_2 += t.statTqk1_2;
			avg.statTqk1_2E += t.statTqk1_2E;
			avg.statTqk10_1 += t.statTqk10_1;
			avg.statTqk10_1E += t.statTqk10_1E;
			avg.statTqk10_2 += t.statTqk10_2;
			avg.statTqk10_2E += t.statTqk10_2E;
			avg.statTu1 += t.statTu1;
			avg.statTu1E += t.statTu1E;
			avg.statTu2 += t.statTu2;
			avg.statTu2E += t.statTu2E;
			avg.statNnodes += t.statNnodes;
			//avg.statNBits += t.statNBits;
			//avg.statNDims += t.statNDims;
			//avg.statNEntries += t.statNEntries;
			avg.statNpostlen += t.statNpostlen;
			avg.statNNodeAHC += t.statNNodeAHC;
			avg.statNNodeNT += t.statNNodeNT;
			avg.statNNodeInternalNT += t.statNNodeInternalNT;
			avg.statNq1 += t.statNq1;
			avg.statNq2 += t.statNq2;
			avg.statNqp1 += t.statNqp1;
			avg.statNqp2 += t.statNqp2;
			avg.statDqk1_1 += t.statDqk1_1;
			avg.statDqk1_2 += t.statDqk1_2;
			avg.statDqk10_1 += t.statDqk10_1;
			avg.statDqk10_2 += t.statDqk10_2;
			avg.statNu1 += t.statNu1;
			avg.statNu2 += t.statNu2;
			avg.statSMin += t.statSMin;
			avg.statSCalc += t.statSCalc;
			avg.statSjvmF += t.statSjvmF;
			avg.statSjvmE += t.statSjvmE;
			avg.statGcDiffL += t.statGcDiffL;
			avg.statGcTimeL += t.statGcTimeL;
			avg.statGcDiffWq += t.statGcDiffWq;
			avg.statGcTimeWq += t.statGcTimeWq;
			avg.statGcDiffPq += t.statGcDiffPq;
			avg.statGcTimePq += t.statGcTimePq;
			avg.statGcDiffUp += t.statGcDiffUp;
			avg.statGcTimeUp += t.statGcTimeUp;
			avg.statGcDiffUl += t.statGcDiffUl;
			avg.statGcTimeUl += t.statGcTimeUl;
			//we just use the info of the last test run
			avg.assortedInfo = t.assortedInfo;
			cnt++;
		}

		avg.statTGen /= (double)cnt;
		avg.statTLoad /= (double)cnt;
		avg.statTUnload /= (double)cnt;
		avg.statTq1 /= (double)cnt;
		avg.statTq1E /= (double)cnt;
		avg.statTq2 /= (double)cnt;
		avg.statTq2E /= (double)cnt;
		avg.statTqp1 /= (double)cnt;
		avg.statTqp1E /= (double)cnt;
		avg.statTqp2 /= (double)cnt;
		avg.statTqp2E /= (double)cnt;
		avg.statTqk1_1 /= (double)cnt;
		avg.statTqk1_1E /= (double)cnt;
		avg.statTqk1_2 /= (double)cnt;
		avg.statTqk1_2E /= (double)cnt;
		avg.statTqk10_1 /= (double)cnt;
		avg.statTqk10_1E /= (double)cnt;
		avg.statTqk10_2 /= (double)cnt;
		avg.statTqk10_2E /= (double)cnt;
		avg.statTu1 /= (double)cnt;
		avg.statTu1E /= (double)cnt;
		avg.statTu2 /= (double)cnt;
		avg.statTu2E /= (double)cnt;
		avg.statNnodes /= (double)cnt;
		//avg.statNBits /= (double)cnt;
		//avg.statNDims /= (double)cnt;
		//avg.statNEntries /= (double)cnt;
		avg.statNpostlen /= (double)cnt;
		avg.statNNodeAHC /= (double)cnt;
		avg.statNNodeNT /= (double)cnt;
		avg.statNNodeInternalNT /= (double)cnt;
		avg.statNq1 /= (double)cnt;
		avg.statNq2 /= (double)cnt;
		avg.statNqp1 /= (double)cnt;
		avg.statNqp2 /= (double)cnt;
		avg.statDqk1_1 /= (double)cnt;
		avg.statDqk1_2 /= (double)cnt;
		avg.statDqk10_1 /= (double)cnt;
		avg.statDqk10_2 /= (double)cnt;
		avg.statNu1 /= (double)cnt;
		avg.statNu2 /= (double)cnt;
		avg.statSMin /= (double)cnt;
		avg.statSCalc /= (double)cnt;
		avg.statSjvmF /= (double)cnt;
		avg.statSjvmE /= (double)cnt;
		avg.statGcDiffL /= (double)cnt;
		avg.statGcTimeL /= (double)cnt;
		avg.statGcDiffWq /= (double)cnt;
		avg.statGcTimeWq /= (double)cnt;
		avg.statGcDiffPq /= (double)cnt;
		avg.statGcTimePq /= (double)cnt;
		avg.statGcDiffUp /= (double)cnt;
		avg.statGcTimeUp /= (double)cnt;
		avg.statGcDiffUl /= (double)cnt;
		avg.statGcTimeUl /= (double)cnt;

		avg.SEEDmsg = "AVG-" + cnt + "/" + stats.size();

		return avg;
	}
	
	@SuppressWarnings("unchecked")
	public Candidate createTree() {
		if (indexClass == null) {
			String className;
			if (isRangeData) {
				className = INDEX.getCandidateClassNameRectangle();
			} else {
				className = INDEX.getCandidateClassNamePoint();
			}
			if (className == null || className.trim().equals("")) {
				throw new IllegalStateException("Please provide a class name "
						+ "for TestStats: " + (INDEX != null ? INDEX.name() : "no index"));
			}
			setCandidateClass(className);
		}
		try {
			Class<Candidate> cls = (Class<Candidate>) indexClass;
			Constructor<Candidate> c = cls.getConstructor(TestStats.class);
			return c.newInstance(this);
		} catch (InstantiationException | IllegalAccessException 
				| IllegalArgumentException | InvocationTargetException 
				| NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setCandidateClass(String className) {
		if (className == null || className.trim().equals("")) {
			throw new IllegalStateException("Please provide a class name "
					+ "for TestStats: " + (INDEX != null ? INDEX.name() : "no index"));
		}
		try {
			indexClass = (Class<Candidate>) Class.forName(className);
		} catch (IllegalArgumentException | SecurityException 
				| ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}