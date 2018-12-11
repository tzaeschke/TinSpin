/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.io.Serializable;

import ch.ethz.globis.tinspin.TestInstances.IDX;
import ch.ethz.globis.tinspin.TestInstances.TST;
import ch.ethz.globis.tinspin.data.TestPointCube;
import ch.ethz.globis.tinspin.wrappers.PointPHC;

public class Example {

	public static void main(String[] args) {
		//How to test your own index or data
		
		//If you test your own index or data, implement one (or two) or the following:
		//1a) Implement an Index into a wrapper class, such as PointPHC.class,
		//    this requires the 'Candidate' class from tinspin-common.
		//1b) Implement a data source into a wrapper, such as TestPointCube,
		//    this requires the 'AbstractTest' class from tinspin-common.
		
		//2) Create an IndexHandle and/or TestHandle
		IndexHandle myIndex = new MyIndexHandle();
		TestHandle myDataSource = new MyDataSource();
		//Alternatively: use predefined index or data source
		//myIndex = IDX.PHC;
		//myDataSource = TST.CUBE_P;
		
		//3) Create TestStats
		int N = 10_000; //10_000 points
		int DIM = 3;    //3 dimensions
		TestStats ts = new TestStats(myDataSource, myIndex, N, DIM, 1.0);

		//4a) Either run it as single test
		//    Features or running TestRunner:
		//    - mainly useful for debugging and superficial tests  
//		TestRunner test = new TestRunner(ts);
//		TestStats s = test.run();
//		System.out.println(s);

		//4b) Or run it with the Framework
		//    Features of running the framework:
		//    - repeated tests with averaged results
		//    - results are written to logfiles in 'target/logs'
		//    - each repeat is run in a separate virtual machine via RMI
		//    - each repeat is warmed up with a small dataset
		ts.cfgNRepeat = 2;
		TestManager.runTest(ts);
	}
	
	private static class MyIndexHandle implements IndexHandle, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public String name() {
			return "MyIndex";
		}
		
		@Override
		public String getCandidateClassNameRectangle() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public String getCandidateClassNamePoint() {
			//This class mus implement 'Candidate'
			//We only need the String name here:
			return PointPHC.class.getName();
		}
	};

	private static class MyDataSource implements TestHandle, Serializable {
		private static final long serialVersionUID = 1L;

		@Override
		public String name() {
			return "MyTest";
		}

		@Override
		public String getTestClassName() {
			//This class must implement 'AbstractTest'
			return TestPointCube.class.getName();
		}

		@Override
		public boolean isRangeData() {
			return false;
		}
	};
	
}
