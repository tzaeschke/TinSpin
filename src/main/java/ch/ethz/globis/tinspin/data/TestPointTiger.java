/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.tiger.TigerKmlIO;

/**
 * Data from Tiger/LINE: http://www2.census.gov/geo/tiger/KML/2010_Proto/2010tract_dt/
 * 
 * @author Tilmann Zaeschke
 */
public class TestPointTiger extends TestPoint {

	private static final String dbName = "TigerKmlArrayND-2D";

	protected TestPointTiger(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestTiger(" + getN() + ")");
		double[] data = TigerKmlIO.readAndBuffer(dbName, S);
		globalMin[0] = TigerKmlIO.minX;
		globalMax[0] = TigerKmlIO.maxX;
		globalMin[1] = TigerKmlIO.minY;
		globalMax[1] = TigerKmlIO.maxY;
		return data;
	}
}
