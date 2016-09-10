/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import static org.junit.Assert.assertEquals;

import ch.ethz.globis.tinspin.TestStats.IDX;
import ch.ethz.globis.tinspin.TestStats.TST;
import ch.ethz.globis.tinspin.wrappers.Candidate;

public abstract class AbstractWrapperTest {

	protected static final int N = 10*1000;
	protected static final int dims = 3;

	public static TestStats createStats(
			String candidate, TST tst, int N, int dims, boolean isRangeData, double param1) {
		TestStats ts = new TestStats(tst, IDX.CUSTOM, N, dims, isRangeData, param1);
		ts.setCandidateClass(candidate);
		ts.cfgNRepeat = 1;
		ts.paramEnforceGC = false;
		ts.cfgWindowQueryRepeat = 100;
		ts.cfgPointQueryRepeat = 1000;
		ts.cfgUpdateSize = 1000;
		return ts;
	}
	
	public static void check(TestStats expected, TestStats ts, Candidate c) {
		
		double EPS = 0.000000001;

		assertEquals(expected.cfgNDims, ts.cfgNDims);
		assertEquals(expected.cfgNEntries, ts.cfgNEntries);
		if (c.supportsWindowQuery()) {
			assertEquals(expected.statNq1, ts.statNq1);
			assertEquals(expected.statNq2, ts.statNq2);
		}
		if (c.supportsPointQuery()) {
			assertEquals(expected.statNqp1, ts.statNqp1);
			assertEquals(expected.statNqp2, ts.statNqp2);
		}
		if (c.supportsUpdate()) {
			assertEquals(expected.statNu1, ts.statNu1);
			assertEquals(expected.statNu2, ts.statNu1);
		}
		if (c.supportsKNN()) {
			assertEquals(expected.statDqk1_1, ts.statDqk1_1, EPS);
			assertEquals(expected.statDqk1_2, ts.statDqk1_2, EPS);
			assertEquals(expected.statDqk10_1, ts.statDqk10_1, EPS);
			assertEquals(expected.statDqk10_2, ts.statDqk10_2, EPS);
		}
	}
	
}
