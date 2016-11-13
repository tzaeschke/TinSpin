/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.osm.OsmRect2D;

/**
 * @author Tilmann Zaeschke
 */
public class TestRectangleOSM extends TestRectangle {

	private static final String dbName = "OsmBufferDB_R";

	protected TestRectangleOSM(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * Read open street map data (ways) from a database.
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestOSM(" + getN() + ")");
		
		//OsmIO io = new OsmIO(DIM);
		//double[] data = io.readAndBuffer(dbName, S);
		OsmRect2D osm = new OsmRect2D();
		double[] data = osm.readAndBuffer(dbName, S);
		globalMin[0] = osm.min()[0];
		globalMax[0] = osm.max()[0];
		globalMin[1] = osm.min()[1];
		globalMax[1] = osm.max()[1];
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
