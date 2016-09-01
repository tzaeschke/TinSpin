/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data.points;

import java.util.Random;

import ch.ethz.globis.phtree.demo.TigerKmlIO;
import ch.ethz.globis.tinspin.MainTest;
import ch.ethz.globis.tinspin.MainTestManager;
import ch.ethz.globis.tinspin.TestStats;

/**
 * Data from Tiger/LINE: http://www2.census.gov/geo/tiger/KML/2010_Proto/2010tract_dt/
 * 
 * @author Tilmann Zaeschke
 */
public class TestPointTiger extends TestPoint {

	private static final String dbName = "TigerKmlArrayND-2D-new";

	protected TestPointTiger(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestTiger(" + N + ")");
		double[] data = TigerKmlIO.readFromDB_Array(dbName);
		return data;
	}

	@Override
	public void queryCuboid(int resultsPerQuery, double[] xyz, double[] len) {
		// Adrien 06/02/2014
		// added for compatibility, only used for TST.CUSTOM tests
	}
}
