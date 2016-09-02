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
 * 
 * @author Tilmann Zaeschke
 */
public class TestRectangleCube extends TestRectangle {

	public static final double MIN_X = 0;
	public static final double MAX_X = 1.0;
	
	public TestRectangleCube(Random R, TestStats S) {
		super(R, S);
	}
	
	
	@Override
	public double[] generate() {
		double RECT_LEN = S.param1;
		log("Running: TestCube (" + RECT_LEN + ")");
		
		int dims = S.statNDims;
		int nEntries = S.statNEntries;
		double[] data = new double[nEntries*dims*2];
		
		//query create cube
		double minRange = MAX_X-MIN_X-RECT_LEN;
		int posMin = 0;
		int posMax = dims;
		for (int i = 0; i < nEntries; i++) {
			for (int d = 0; d < dims; d++) {
				data[posMin++] = MIN_X+R.nextDouble()*minRange;
				data[posMax++] = data[posMin-1]+R.nextDouble()*RECT_LEN;
			}
			posMin += dims;
			posMax += dims;
		} 
		return data;
	}
	

	@Override
	public double maxUpdateDistance() {
		return S.param1/100;
	}


	@Override
	public void queryCuboid(int resultsPerQuery, double[] min, double[] max) {
		throw new UnsupportedOperationException();
	}
}
