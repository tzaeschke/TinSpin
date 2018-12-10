/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.phtree.util.PhTreeStats;
import ch.ethz.globis.tinspin.TestStats;

public abstract class CandidatePHC extends Candidate {
	

	public void setStats(TestStats S, PhTreeStats q) {
		//Nothing to do for trees other than PhTrees
		S.statNnodes = q.getNodeCount();
		S.statSCalc = q.getCalculatedMemSize();
		S.statNpostlen = (long) q.getAvgPostlen(null);
		S.statNNodeAHC = q.getAhcCount();
		S.statNNodeNT = q.getNtCount();
		S.statNNodeInternalNT = q.getNtInternalNodeCount();
		S.cfgNBits = q.getBitDepth();
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

	
}
