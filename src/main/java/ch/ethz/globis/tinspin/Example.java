/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin;

import java.io.Serializable;

import ch.ethz.globis.tinspin.wrappers.PointPHC;

public class Example {

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
			//We only need the String name here:
			return PointPHC.class.getName();
		}
	};

	
	public static void main(String[] args) {
		//How to test your own index
		
		//1) Implement an Index into a wrapper class, such as PointPHC.class,
		//   this requires the 'Candidate' class from tinspin-common.
		
		//2) Create an IndexHandle
		IndexHandle myIndex = new MyIndexHandle();
		
		//3) Create TestStats
		int N = 10_000; //10_000 points
		int DIM = 3;    //3 dimensions
		TestStats ts = new TestStats(TestInstances.TST.CUBE_P, myIndex, N, DIM, 1.0);

		//4a) Either run it as single test:
		//    Features: This is mainly useful for debugging and superficial tests  
//		TestRunner test = new TestRunner(ts);
//		TestStats s = test.run();
//		System.out.println(s);

		//4b) Or run it with the Framework
		//    Features of running the framework:
		//    - repeated tests with averaged results
		//    - results are written to logfiiles in 'target/logs'
		//    - each repeat is run in a separate virtual machine via RMI
		//    - each repeat is warmed up with a small dataset
		ts.cfgNRepeat = 2;
		TestManager.runTest(ts);
	}
	
}
