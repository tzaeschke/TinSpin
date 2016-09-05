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
 * Creates a k-dimensional cuboid between 0.0 and 'param1' in every dimension,
 * randomly filled with points.
 * 
 * @author Tilmann Zaeschke
 */
public class TestPointCube extends TestPoint {

	public TestPointCube(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestCube(" + param1 + ")");
		double[] data = new double[getN()*DIM];
		for (int i = 0; i < getN(); i++) {
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
