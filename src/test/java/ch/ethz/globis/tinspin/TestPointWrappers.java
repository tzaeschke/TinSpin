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
public class TestPointWrappers extends AbstractWrapperTest {

	private final String candidate;
	
	private static TestStats expectedCube = null;
	private static TestStats expectedCluster = null;
	
	public TestPointWrappers(String candCls) {
		this.candidate = candCls;
	}
	
	@BeforeClass
	public static void beforeClass() {
		String candidate = IDX.ARRAY.getCandidateClassNamePoint();
		//init results
		//use this as reference for all others
		//if the naive implementation should be wrong, the others should fail as well
		expectedCube = createUnitTestStats(candidate, TST.CUBE_P, N, dims, 1.0);
		new TestRunner(expectedCube).run();

		expectedCluster = createUnitTestStats(candidate, TST.CLUSTER_P, N, dims, 5.0);
		new TestRunner(expectedCluster).run();
	}
	
	@Parameters
	public static Iterable<Object[]> candidates() {
		ArrayList<Object[]> l = new ArrayList<>();
		l.add(new Object[]{IDX.ARRAY.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHC.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHC2.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHCF.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.PHC_IPP.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.QTZ.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.QT2Z.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.RSZ.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.STRZ.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.CBZ.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.KDZ.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.CTZ.getCandidateClassNamePoint()});
		return l;
	}

	@Test
	@Parameters
    public void testCube() {
		TestStats ts = createUnitTestStats(candidate, TST.CUBE_P, N, dims, 1.0);
		TestRunner tr = new TestRunner(ts);
		tr.run();

		check(expectedCube, ts, tr.getCandidate());
	}
	
	@Test
	@Parameters
    public void testCluster() {
		TestStats ts = createUnitTestStats(candidate, TST.CLUSTER_P, N, dims, 5.0);
		TestRunner tr = new TestRunner(ts);
		tr.run();

		check(expectedCluster, ts, tr.getCandidate());
	}
}
