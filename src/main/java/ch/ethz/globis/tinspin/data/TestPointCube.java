/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;

/**
 * [1] L. Arge, M. de Berg, H. J. Haverkort and K. Yi: 
 * "The Priority R-Tree: A Practically Efficient and Worst-Case Optimal R-Tree"
 * 
 * @author Tilmann Zaeschke
 */
public class TestPointCube extends TestPoint {

	public TestPointCube(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * From [1]:
	 * skewed(c): In many real-life multidimensional datasets
	 * different dimensions often have different distributions,
	 * some of which may be highly skewed compared
	 * to the others. We designed the third class of
	 * datasets to investigate how this affects R-tree performance.
	 * skewed(c) consists of uniformly distributed
	 * points that have been 'squeezed' in the y-dimension,
	 * that is, each point (x, y) is replaced with (x, y^c). An
	 * example of skewed(5) is shown in Figure 7.
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestSkewed(" + param1 + ")");
		double[] data = new double[N*DIM];
		for (int i = 0; i < N; i++) {
			int pos = DIM*i;
			for (int d = 0; d < DIM; d++) {
				data[pos+d] = R.nextDouble() * param1;
			}
		}
		return data;
	}

	@Override
	public void queryCuboid(int resultsPerQuery, double[] xyz, double[] len) {
		// Adrien 06/02/2014
		// added for compatibility, only used for TST.CUSTOM tests
	}
}
