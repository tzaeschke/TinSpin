/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.osm.Osm2D;

/**
 * @author Tilmann Zaeschke
 */
public class TestPointOpenStreetMap extends TestPoint {

	private static final String dbName = "OsmBufferDB";

	protected TestPointOpenStreetMap(Random R, TestStats S) {
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
		Osm2D osm = new Osm2D();
		double[] data = osm.readAndBuffer(dbName, S);
		return data;
	}

	@Override
	public void queryCuboid(int resultsPerQuery, double[] xyz, double[] len) {
		// Adrien 06/02/2014
		// added for compatibility, only used for CUSTOM tests
	}
}
