/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Random;

import ch.ethz.globis.phtree.pre.ColumnType;
import ch.ethz.globis.tinspin.TestStats;

/**
 * @author Tilmann Zaeschke
 */
public class TestPointOpenStreetMap extends TestPoint {

	protected TestPointOpenStreetMap(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * Read open street map data from a database.
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestOSM(" + param1 + ")");
		double LEN = 1.0;
		double[] data = new double[N*DIM];
		
		
		
		//With  x_L = x_1*LEN
		//Using LEN!=0.1, we have to calculate with x_L=(x_1/LEN)^c * LEN
		// -->  x_L = x_1^c * (1/LEN)^c * LEN
		double lcl = LEN / Math.pow(LEN, param1);

		for (int i = 0; i < N; i++) {
			int pos = DIM*i;
			for (int d = 0; d < DIM; d++) {
				data[pos+d] = R.nextDouble();
				if (d==1) {
					data[pos+d] = LEN * lcl * Math.pow(data[pos+d], param1);
				} else {
					data[pos+d] *= LEN;
				}
			}
		}
		return data;
	}

	@Override
	public void queryCuboid(int resultsPerQuery, double[] xyz, double[] len) {
		// Adrien 06/02/2014
		// added for compatibility, only used for CUSTOM tests
	}
	
	@Override
	public ColumnType[] getColumnTypes() {
		ColumnType[] types = new ColumnType[]{
				new ColumnType.LongColumn(),
				new ColumnType.LongColumn(),
				new ColumnType.LongColumn(),
				new ColumnType.IntColumn(),
				new ColumnType.IntColumn(), 
				new ColumnType.IntColumn(),
				new ColumnType.LongColumn(),
		};
		return types;
	}
}
