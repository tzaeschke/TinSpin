/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.tiger.TigerKmlPoint2D;

/**
 * Data from Tiger/LINE: http://www2.census.gov/geo/tiger/KML/2010_Proto/2010tract_dt/
 * 
 * @author Tilmann Zaeschke
 */
public class TestPointTiger extends TestPoint {

	private static final String dbName = "TigerKmlPoint-2D";

	protected TestPointTiger(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestTiger(" + getN() + ")");
		double[] data = TigerKmlPoint2D.readAndBuffer(dbName, S);
		globalMin[0] = TigerKmlPoint2D.minX;
		globalMax[0] = TigerKmlPoint2D.maxX;
		globalMin[1] = TigerKmlPoint2D.minY;
		globalMax[1] = TigerKmlPoint2D.maxY;
		return data;
	}
}
