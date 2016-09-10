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

import ch.ethz.globis.tinspin.TestStats.IDX;
import ch.ethz.globis.tinspin.TestStats.TST;

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
		expectedCube = createStats(candidate, TST.CUBE, N, dims, false, 1.0);
		new TestRunner(expectedCube).run();

		expectedCluster = createStats(candidate, TST.CLUSTER, N, dims, false, 3.4);
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
		l.add(new Object[]{IDX.PHC2_IPP.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.QKDZ.getCandidateClassNamePoint()});
		l.add(new Object[]{IDX.CBZ.getCandidateClassNamePoint()});
		return l;
	}

	@Test
	@Parameters
    public void testCube() {
		TestStats ts = createStats(candidate, TST.CUBE, N, dims, false, 1.0);
		TestRunner tr = new TestRunner(ts);
		tr.run();

		check(expectedCube, ts, tr.getCandidate());
	}
	
	@Test
	@Parameters
    public void testCluster() {
		TestStats ts = createStats(candidate, TST.CLUSTER, N, dims, false, 3.4);
		TestRunner tr = new TestRunner(ts);
		tr.run();

		check(expectedCluster, ts, tr.getCandidate());
	}
}
