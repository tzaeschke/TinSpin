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
public class TestRectangleTiger extends TestRectangle {

	private static final String dbName = "TigerKmlArrayND-4D";

	private double[] data; 
	
	public TestRectangleTiger(Random R, TestStats S) {
		super(R, S);
	}
	
	
	/**
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestTiger(" + getN() + ")");
		data = TigerKmlIO.readFromDB_Array(dbName);
		for (int i = 0; i < getN(); i++) {
			TigerKmlIO.minMax( data[i*4  ], data[i*4+1] );
			TigerKmlIO.minMax( data[i*4+2], data[i*4+3] );
			if (data[i*4  ] > data[i*4+2]) {
				double t = data[i*4  ];
				data[i*4  ] = data[i*4+2];
				data[i*4+2] = t;
			}
			if (data[i*4+1] > data[i*4+3]) {
				double t = data[i*4+1];
				data[i*4+1] = data[i*4+3];
				data[i*4+3] = t;
			}
		}
		globalMin[0] = TigerKmlIO.minX;
		globalMax[0] = TigerKmlIO.maxX;
		globalMin[1] = TigerKmlIO.minY;
		globalMax[1] = TigerKmlIO.maxY;
		return data;
	}

	@Override
	public void generateQuery(double[] min, double[] max, 
			final double maxLen, final double avgQVol) {
		//queries should in average return 1000 results
		//double ratio = 18300000.0/MAX_E * 1.0/18300;
		double ratio = Math.sqrt(S.cfgWindowQuerySize/(double)getN());  //ratio for each axis
		//double ratio = 0.001; 
		
		//create rectangles with 0.1% coverage
		double alpha = R.nextDouble() * Math.PI/2;
		double len1 = Math.sqrt(Math.tan(alpha))*ratio*(TigerKmlIO.maxX-TigerKmlIO.minX); 
		double len0 = Math.sqrt(1/Math.tan(alpha))*ratio*(TigerKmlIO.maxY-TigerKmlIO.minY); 
		
//		double rl = R.nextDouble(); 
//		double len0 = rl*ratio*(TigerKmlIO.maxX-TigerKmlIO.minX);
//		double len1 = ratio*rl*(TigerKmlIO.maxY-TigerKmlIO.minY);
		
		min[0] = R.nextDouble()*(TigerKmlIO.maxX - TigerKmlIO.minX - len0) + TigerKmlIO.minX;
		min[1] = R.nextDouble()*(TigerKmlIO.maxY - TigerKmlIO.minY - len1) + TigerKmlIO.minY;
		max[0] = min[0] + len0;
		max[1] = min[1] + len1;
		
		if (DIM == 3) {
			min[2] = 0;
			max[2] = 1;
		}
		//System.out.println("l0/l1= " + (len0/len1) + "  min=" + Arrays.toString(min) + " max=" + Arrays.toString(max));
	}
	
	/**
	 * 
	 * @return Maximum distance for each update().
	 */
	@Override
	public double maxUpdateDistance() {
		return 0.1;
	}
}
