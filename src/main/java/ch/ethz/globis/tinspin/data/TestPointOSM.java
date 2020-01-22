/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.osm.OsmPoint2D;

/**
 * @author Tilmann Zaeschke
 */
public class TestPointOSM extends TestPoint {

	private static final String dbName = "OsmBufferDB_P";

	public TestPointOSM(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * Read open street map data from a database.
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestOSM(" + getN() + ")");
		
		//OsmIO io = new OsmIO(DIM);
		//double[] data = io.readAndBuffer(dbName, S);
		OsmPoint2D osm = new OsmPoint2D();
		double[] data = osm.readAndBuffer(dbName, S);
		globalMin[0] = osm.min()[0];
		globalMax[0] = osm.max()[0];
		globalMin[1] = osm.min()[1];
		globalMax[1] = osm.max()[1];
		return data;
	}
}
