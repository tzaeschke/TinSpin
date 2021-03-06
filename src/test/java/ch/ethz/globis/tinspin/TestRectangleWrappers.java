/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import ch.ethz.globis.tinspin.TestInstances.IDX;
import ch.ethz.globis.tinspin.TestInstances.TST;

@RunWith(Parameterized.class)
public class TestRectangleWrappers extends AbstractWrapperTest {

	private final String candidate;
	
	private static TestStats expectedCube = null;
	private static TestStats expectedCluster = null;

	public TestRectangleWrappers(String candCls) {
		this.candidate = candCls;
	}
	
	@BeforeClass
	public static void beforeClass() {
		String candidate = IDX.ARRAY.getCandidateClassNameRectangle();
		//init results
		//use this as reference for all others
		//if the naive implementation should be wrong, the others should fail as well
		expectedCube = createUnitTestStats(candidate, TST.CUBE_R, N, dims, 1.0);
		new TestRunner(expectedCube).run();

		expectedCluster = createUnitTestStats(candidate, TST.CLUSTER_R, N, dims, 5.0);
		new TestRunner(expectedCluster).run();
	}
	
	@Parameters
	public static Iterable<Object[]> data1() {
		ArrayList<Object[]> l = new ArrayList<>();
		l.add(new Object[]{IDX.ARRAY.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.PHC.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.PHC2.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.PHCF.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.PHC_IPP.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.RSZ.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.STRZ.getCandidateClassNameRectangle()});
		l.add(new Object[]{IDX.QTZ.getCandidateClassNameRectangle()});
		return l;
	}

	@Test
	@Parameters
    public void testCube() {
		//0.00001 is the default size of the rectangles
		TestStats ts = createUnitTestStats(candidate, TST.CUBE_R, N, dims, 1.0);
		TestRunner tr = new TestRunner(ts);
		tr.run();
		
		check(expectedCube, ts, tr.getCandidate());
	}
	
	@Test
	@Parameters
    public void testCluster() {
		TestStats ts = createUnitTestStats(candidate, TST.CLUSTER_R, N, dims, 5.0);
		TestRunner tr = new TestRunner(ts);
		tr.run();

		check(expectedCluster, ts, tr.getCandidate());
	}
	
}
