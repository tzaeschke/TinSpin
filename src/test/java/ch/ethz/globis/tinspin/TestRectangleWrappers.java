/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.util.ArrayList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.ethz.globis.tinspin.TestStats.IDX;
import ch.ethz.globis.tinspin.TestStats.TST;

@RunWith(Parameterized.class)
public class TestRectangleWrappers {

	private static final int N = 100*1000;

	private final String candidate;
	
	public TestRectangleWrappers(String candCls) {
		this.candidate = candCls;
	}
	
	@Parameters
	public static Iterable<Object[]> data1() {
		ArrayList<Object[]> l = new ArrayList<>();
		l.add(new Object[]{IDX.PHC.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.PHC2.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.PHCF.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.PHC_IPP.getCandidateClassNameRectangle()});
		//l.add(new Object[]{IDX.QKDZ.getCandidateClassNameRectangle()});
		return l;
	}

	@Test
	@Parameters
    public void testCube() {
		//0.00001 is the default size of the rectangles
		TestStats ts = new TestStats(TST.CUBE, IDX.CUSTOM, N, 3, true, 1.0);
		ts.setCandidateClass(candidate);
		ts.cfgNRepeat = 1;
		ts.paramUseGC = false;
		ts.cfgWindowQueryRepeat = 100;
		TestRunner tr = new TestRunner(ts);
		tr.run();

		//System.out.println(ts);
		
		//repeat == 1; NQ = 100
		//CUSTOM-R-0	CUBE(1.0E-5, 0.0, null)	3	64	100000	0	8	0	0	37	170	539	
		//492	401	314	736	652	116000	78000	150000	142000	44999	48	0	0	0	
		//100156	99470	500038	500116	250000	250000	
		//0.22589505297599916	0.22406998268552575	0.2277830659348768	0.215077012049881	
		//96	1700	960	0	0	0	0	0	0	-131	6	-134	5	0	0	0	0	
		//PhTree11 AHC/LHC=2.0 AHC-on=true HCI-on=true NtLimit=150 NtMaxDim=6 
		//DEBUG=false WINDOW_RESULTS=1000 KNN_REPEAT=1000

		
		TestPointWrappers.checkValue(ts, tr.getCandidate(), 3, N, 
				100156, 99470, 500038, 500116, 250000, 250000,
				0.22589505297599916, 0.22406998268552575, 
				0.2277830659348768, 0.215077012049881);
	}
	
	@Test
	@Parameters
    public void testCluster() {
		TestStats ts = new TestStats(TST.CLUSTER, IDX.CUSTOM, N, 3, true, 3.4);
		ts.setCandidateClass(candidate);
		ts.cfgNRepeat = 1;
		ts.paramUseGC = false;
		TestRunner tr = new TestRunner(ts);
		tr.run();

		//Repeat = 1
		//CUSTOM-R-0	CLUSTER(3.4, 0.0, null)	3	64	100000	0	9	0	0	21	149	1501	
		//1204	419	408	1168	1068	12750000	10900000	14250000	11800000	68073	
		//36	0	0	0	
		//9992	9965	500046	500106	250000	250000	
		//0.6400297095966576	0.7475088161602411	0.7190152445255513	0.7391261342771682	
		//73	1490	730	0	0	0	0	0	0	0	0	0	0	-134	4	0	0	
		//PhTree11 AHC/LHC=2.0 AHC-on=true HCI-on=true NtLimit=150 NtMaxDim=6 DEBUG=false WINDOW_RESULTS=1000 KNN_REPEAT=20

		TestPointWrappers.checkValue(ts, tr.getCandidate(), 3, N,
				9992, 9965, 500046, 500106, 250000, 250000, 
				0.6400297095966576, 0.7475088161602411, 
				0.7190152445255513, 0.7391261342771682);	
				
	}
	
}
