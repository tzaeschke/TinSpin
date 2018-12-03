/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.TestPoint;

/**
 * @author Tilmann Zaeschke
 */
public class TestPointHDF5 extends TestPoint {

	//File source:
	//https://github.com/erikbern/ann-benchmarks/blob/master/README.md
	
	public TestPointHDF5(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * Read open street map data from a database.
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		paramStr = "D:\\data\\HDF5\\";
		switch ((int)param1) {
		case 1: 
			paramStr += "fashion-mnist-784-euclidean.hdf5"; break;
		case 2:
			paramStr += "glove-25-angular.hdf5"; break;
		case 3: 
			paramStr += "sift-128-euclidean.hdf5"; break;
		case 4: 
			paramStr += "nytimes-256-angular.hdf5"; break;
		default:
			throw new IllegalArgumentException("param1 must be {1,2,3,4}");	
		}
		
		//Name of dataset
		paramStr2 = "train";
		
		log("Running: TestHDF5(" + paramStr + "," + paramStr2 + ","  + 
				S.cfgNEntries + "," + S.cfgNDims + ")");
		
		Reader r = Reader.createReader(paramStr);
		HDF5Dataset dataset = r.findDataset(paramStr2);
		double[] data = dataset.getDatasetAsDoubleArray(S.cfgNEntries, true);
		System.out.println("Dataset: " + dataset.getName() + 
				";  dim/cnt=" + dataset.getDims() + "/" + dataset.getCount() + 
				";  size=" + data.length);
		r.close();

		int dims = dataset.getDims();
		int count = data.length/dims;
		this.globalMin = new double[dims];
		this.globalMax = new double[dims];
		this.DIM = dims;
		this.S.cfgNDims = dims;
		this.S.cfgNEntries = count;
		
		log("Running (effective): TestHDF5(" + paramStr + "," + paramStr2 + "," + 
				S.cfgNEntries + "," + S.cfgNDims + ")");

		int pos = 0;
		for (int c = 0; c < count; c++) {
			for (int d = 0; d < dims; d++) {
				globalMin[d] = Math.min(globalMin[d], data[pos]);
				globalMax[d] = Math.max(globalMax[d], data[pos]);
				pos++;
			}
		}
		return data;
	}
}
