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
		TestStats ts = new TestStats(TST.CUBE, IDX.CUSTOM, N, 3, true, 1.0);
		ts.setCandidateClass(candidate);
		ts.cfgNRepeat = 1;
		ts.paramUseGC = false;
		ts.cfgWindowQueryRepeat = 100;
		TestRunner tr = new TestRunner(ts);
		tr.run();

		System.out.println(ts);
		
		//repeat == 1; NQ = 100
		//CUSTOM-R-0	CUBE(1.0, 0.0, null)	3	64	100000	0	8	0	0	49	188	169	
		//152	370	260	2368	1768	187000	105000	218000	219000	45159	48	0	0	0	
		//2353466	2413260	500038	500116	250000	250000	
		//0.22606618565438802	0.22457583937435036	0.2283712876619878	0.21535341030428845	
		//98	1880	980	0	0	0	0	0	0	-125	7	0	0	-134	3	0	0	
		//PhTree11 AHC/LHC=2.0 AHC-on=true HCI-on=true NtLimit=150 NtMaxDim=6 
		//DEBUG=false WINDOW_RESULTS=1000 KNN_REPEAT=1000

		TestPointWrappers.checkValue(ts, tr.getCandidate(), 3, N, 
				2353466, 2413260, 500038, 500116, 250000, 250000,
				0.22606618565438802, 0.22457583937435036, 
				0.2283712876619878, 0.21535341030428845);
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
