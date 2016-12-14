/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.tiger.TigerKmlRect2D;

/**
 * Data from Tiger/LINE: http://www2.census.gov/geo/tiger/KML/2010_Proto/2010tract_dt/
 * 
 * @author Tilmann Zaeschke
 */
public class TestRectangleTiger extends TestRectangle {

	private static final String dbName = "TigerKmlRect-2D";

	
	public TestRectangleTiger(Random R, TestStats S) {
		super(R, S);
	}
	
	
	/**
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestTiger(" + getN() + ")");
		double[] data = TigerKmlRect2D.readAndBuffer(dbName, S);
		globalMin[0] = TigerKmlRect2D.minX;
		globalMax[0] = TigerKmlRect2D.maxX;
		globalMin[1] = TigerKmlRect2D.minY;
		globalMax[1] = TigerKmlRect2D.maxY;
		return data;
	}


	@Override
	public double maxUpdateDistance() {
		double d = globalMax[0]-globalMin[0];
		d /= 10000;
		if (d < 0.00000001) {
			throw new IllegalStateException("d=" + d);
		}
		return d;
	}
}
