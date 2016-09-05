/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.ethz.globis.tinspin.TestStats.IDX;
import ch.ethz.globis.tinspin.TestStats.TST;
import ch.ethz.globis.tinspin.wrappers.Candidate;

@RunWith(Parameterized.class)
public class TestPointWrappers {

	private static final int N = 100*1000;

	private final String candidate;
	
	public TestPointWrappers(String candCls) {
		this.candidate = candCls;
	}
	
	@Parameters
	public static Iterable<Object[]> data1() {
		ArrayList<Object[]> l = new ArrayList<>();
		l.add(new Object[]{IDX.PHC.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHC2.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHCF.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHC_IPP.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHC2_IPP.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.QKDZ.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.CBZ.getCandidateClassNamePoint()});
		return l;
	}

	@Test
	@Parameters
    public void testCube() {
		TestStats ts = new TestStats(TST.CUBE, IDX.CUSTOM, N, 3, false, 1.0);
		ts.setCandidateClass(candidate);
		ts.cfgNRepeat = 1;
		ts.paramUseGC = false;
		TestRunner tr = new TestRunner(ts);
		tr.run();

		//repeat == 1!!!
		//PHC-P-0	CUBE(1.0, 0.0, null)	3	64	100000	0	6	15434744	154	12	
		//167	181	121	223	224	612	364	19000	17000	101000	83000	45172	48	12626	0	0	
		//998672	998712	500190	500170	250000	250000	
		//0.011816015381666765	0.011796872589263036	0.022372481118893204	0.022264012955090375
		//64	1670	640	0	0	0	0	0	0	-150	1	0	0	0	0	-136	2	
		//PhTree11 AHC/LHC=2.0 AHC-on=true HCI-on=true NtLimit=150 NtMaxDim=6 
		//DEBUG=false WINDOW_RESULTS=1000 KNN_REPEAT=1000
		checkValue(ts, tr.getCandidate(), 3, N, 
				998672, 998712, 500190, 500170, 250000, 250000,
				0.011816015381666765, 0.011796872589263036, 
				0.022372481118893204, 0.022264012955090375);
	}
	
	@Test
	@Parameters
    public void testCluster() {
		TestStats ts = new TestStats(TST.CLUSTER, IDX.CUSTOM, N, 3, false, 3.4);
		ts.setCandidateClass(candidate);
		ts.cfgNRepeat = 1;
		ts.paramUseGC = false;
		TestRunner tr = new TestRunner(ts);
		tr.run();

		//Repeat = 1
		//CUSTOM-P-0	CLUSTER(3.4, 0.0, null)	
		//3	64	100000	0	7	16773624	167	42	169	2397	697	326	303	1632	1256	
		//9650000	10100000	7500000	6650000	68312	36	8537	0	0	
		// 10009	10042 	500153	500213	250000	250000	
		//0.37276245758400767	0.40834355011150236	0.40292023886401107	0.4114167231935806	
		//63	1690	630	0	0	0	0	0	0	-127	5	0	0	0	0	0	0	
		//PhTree11 AHC/LHC=2.0 AHC-on=true HCI-on=true NtLimit=150 NtMaxDim=6 
		//DEBUG=false WINDOW_RESULTS=1000 KNN_REPEAT=20

		checkValue(ts, tr.getCandidate(), 3, N, 
				10009, 10042, 500153, 500213, 250000, 250000,
				0.37276245758400767, 0.40834355011150236, 
				0.40292023886401107, 0.4114167231935806);
	}
	
	public static void checkValue(TestStats ts, Candidate c, int dims, int nEntries,
			int nq1, int nq2, int np1, int np2, int nu1, int nu2,
			double dqk1_1, double dqk1_2, double dqk10_1, double dqk10_2) {
		
		double EPS = 0.00000000001;

		assertEquals(dims, ts.cfgNDims);
		assertEquals(nEntries, ts.cfgNEntries);
		if (c.supportsWindowQuery()) {
			assertEquals(nq1, ts.statNq1);
			assertEquals(nq2, ts.statNq2);
		}
		if (c.supportsPointQuery()) {
			assertEquals(np1, ts.statNqp1);
			assertEquals(np2, ts.statNqp2);
		}
		if (c.supportsUpdate()) {
			assertEquals(nu1, ts.statNu1);
			assertEquals(nu2, ts.statNu1);
		}
		if (c.supportsKNN()) {
			assertEquals(dqk1_1, ts.statDqk1_1, EPS);
			assertEquals(dqk1_2, ts.statDqk1_2, EPS);
			assertEquals(dqk10_1, ts.statDqk10_1, EPS);
			assertEquals(dqk10_2, ts.statDqk10_2, EPS);
		}
	}
	
}
