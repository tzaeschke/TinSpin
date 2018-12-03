/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Arrays;
import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;

public abstract class AbstractTest {

	protected final Random R;
	protected int DIM;
	protected final double param1;
	protected final double param2;
	protected String paramStr;
	protected String paramStr2;
	protected final TestStats S;
	//global min/max of data area
	protected double[] globalMin;
	protected double[] globalMax;

	protected AbstractTest(Random R, TestStats S) {
		this.R = R;
		this.S = S;
		this.DIM = S.cfgNDims;
		this.param1 = S.param1;
		this.param2 = S.param2;
		this.paramStr = S.paramStr;
		this.paramStr2 = S.paramStr2;
		this.globalMin = new double[DIM];
		this.globalMax = new double[DIM];
		Arrays.fill(globalMin, 0);
		Arrays.fill(globalMax, S.cfgDataLen);
	}

	public int getN() {
		//This may change, for example when exceeding maximum size for
		//CSV, Tiger, OpenStreeMap datasets
		return S.cfgNEntries;
	}

	public final TestStats.TST getTestType() {
		return S.TEST;
	}

	/**
	 * 
	 * @return Maximum distance for each update().
	 */
	public double maxUpdateDistance() {
		double d = 0;
		for (int i = 0; i < globalMin.length; i++) {
			if (d < globalMax[i]-globalMin[i]) {
				d = globalMax[i]-globalMin[i];
			}
		}
		return d * 10e-5;
	}

	public abstract double[] generate();
	public abstract double[][] generateUpdates(int n, double[] data, double[][] ups);
	
	public void log(String str) {
		System.out.println(str);
	}
	
	public void logWarning(String str) {
		System.err.println(str);
	}
	
	public TestStats getTestStats() {
		return S;
	}
	
	public void generateWindowQueries(double[][] lower, double[][] upper) {
		int nEntries = S.cfgNEntries;
		if (nEntries < S.cfgWindowQuerySize*10) {
			//N < 10*000 ? -> N = 100
			nEntries = S.cfgWindowQuerySize*10;
		}

		//find minimum extent (maximum allowable query box size)
		double maxQueryLen = Double.MAX_VALUE;
		//Here is a fixed size version, returning 1% of the space.
		//final double qVolume = 0.01 * Math.pow(cfgDataLen, DIM);//(float) Math.pow(0.1, DIM); //0.01 for DIM=2
		double[] maxLen = new double[DIM];
		double totalV = 1;
		for (int i = 0; i < DIM; i++) {
			double len = globalMax[i] - globalMin[i];
			maxLen[i] = len;
			totalV *= len;
			maxQueryLen = len < maxQueryLen ? len : maxQueryLen;  
		}

		int dims = DIM;
		double[] maxLenProduct = new double[dims];
		maxLenProduct[dims-1] = maxLen[dims-1]; 
		for (int i = dims - 2; i >=0; i--) {
			maxLenProduct[i] = maxLenProduct[i+1]*maxLen[i];
		}

		while (S.cfgWindowQuerySize*100 > nEntries) {
			S.cfgWindowQuerySize /= 10;
		}
		
		double avgQueryVolume = S.cfgWindowQuerySize/(double)nEntries * totalV;
		//final double avgLen = Math.pow(avgVolume, 1./DIM);
		//final double avgLenVar = 0.5*avgLen;
		//final double minLen = 0.5*avgLen;
		
		//TODO?
		//Ignore checks if 
		boolean ignoreChecks = avgQueryVolume * 1000 > totalV; 
		for (int i = 0; i < lower.length; i++) {
			generateQuery3(lower[i], upper[i], maxLen, maxLenProduct, avgQueryVolume,
					ignoreChecks);
		}
	}
	
	/**
	 * Generate query rectangle.
	 * This method should be overwritten by tests that provide
	 * non-standard queries.
	 * @param min output: query box minimum
	 * @param max output: query box maximum
	 * @param maxLen maximum allowed length for a query box in any dimension.
	 * @param avgQVol Average expected volume of a query box
	 */
	public void generateQuery(double[] min, double[] max, 
			final double maxLen, final double avgQVol) {
		if (DIM > 40) {
			//DIM > 40 is problematic, so we use a different algorithm
			generateQuery2(min, max, maxLen, avgQVol);
			return;
		}
		int dims = DIM;
		
		//query create cube
		double[] len = new double[min.length];
		int nTries = 0;
		do {
			double vol = 1;
			for (int d = 0; d < dims-1; d++) {
				//calculate the average required len 
				final double avgLen = Math.pow(avgQVol/vol, 1./dims);
				//create a len between 0.5 and 1.5 of the required length
				len[d] = (0.5*avgLen) + R.nextDouble()*(avgLen);
				len[d] = len[d] > maxLen*0.99 ? maxLen*0.99 : len[d];
				vol *= len[d];
			}
			
			//create cuboid/box of desired size by dropping random length
			len[dims-1] = avgQVol/vol;  //now the new len creates a rectangle/box of SIZE.
			//System.out.println("XXX2 " + Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
			if (nTries++ > 100) {
				System.out.println(Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
				throw new IllegalStateException("dims=" + dims + "  N=" + S.cfgNEntries);
			}
		} while (len[dims-1] >= maxLen); //drop bad rectangles
		
		shuffle(len);
		
		//create location
		for (int d = 0; d < dims; d++) {
			min[d] = globalMin[d] + R.nextDouble()*(maxLen-len[d]);
			max[d] = min[d]+len[d];
			if (min[d]+len[d] >= globalMax[d]) {
				//drop bad rectangles 
				throw new RuntimeException();
			}
		}
	}

	/**
	 * Generate query rectangle. THis is an improved version of the faulty 
	 * {@link #generateQuery(double[], double[], double, double)}.
	 * 
	 * This method should be overwritten by tests that provide non-standard queries.
	 * @param min output: query box minimum
	 * @param max output: query box maximum
	 * @param maxLen maximum allowed length for a query box in any dimension.
	 * @param avgQVol Average expected volume of a query box
	 */
	public void generateQuery2(double[] min, double[] max, 
			final double maxLen, final double avgQVol) {
		int dims = min.length;
		
		//query create cube
		double[] len = new double[min.length];
		int nTries = 0;
		double vol = 1;
		do {
			for (int d = 0; d < dims-1; d++) {
				//calculate the average required len 
				final double avgLen = Math.pow(avgQVol/vol, 1./(dims-d));
				//Can't be smaller than that if other lengths should be < maxLen
				final double minLen = avgQVol/(vol*Math.pow(maxLen,  dims-1-d));  
				double variationUp = maxLen-avgLen; // MAX - AVG
				double variationDown = avgLen-minLen; // AVG - MIN
				//0.999 to avoid queries outside the data domain
				double variation = Math.min(variationUp, variationDown) * 0.999;  
				len[d] = avgLen + (R.nextDouble()*2-1.)*variation;
				if (len[d] > 1000 || Double.isNaN(len[d]) || len[d] <= 0) {
					System.out.println(Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
				}
				vol *= len[d];
			}
			
			//create cuboid/box of desired size by dropping random length
			len[dims-1] = avgQVol/vol;  //now the new len creates a rectangle/box of SIZE.
			//System.out.println("XXX " + Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
			if (nTries++ > 3) {
				System.out.println(Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
				throw new IllegalStateException("dims=" + dims + "  N=" + S.cfgNEntries);
			}
		} while (len[dims-1] >= maxLen); //drop bad rectangles
		
		shuffle(len);
		
		//create location
		for (int d = 0; d < dims; d++) {
			min[d] = globalMin[d] + R.nextDouble()*(maxLen-len[d]);
			max[d] = min[d]+len[d];
			if (min[d]+len[d] >= globalMax[d]) {
				//drop bad rectangles 
				throw new RuntimeException("m/l/m=" + min[d] + "/" + len[d] + "/" + max[d] + 
						"; gm/gm=" + globalMin[d] + "/" + globalMax[d]);
			}
		}
	}

	/**
	 * Generate query rectangle. This is an improved version of the faulty 
	 * {@link #generateQuery2(double[], double[], double, double)},
	 * which did not work well with natural datasets (not cuboid).
	 * 
	 * This method should be overwritten by tests that provide non-standard queries.
	 * @param min output: query box minimum
	 * @param max output: query box maximum
	 * @param maxLen maximum allowed length for a query box in any dimension.
	 * @param maxLenProduct Increasing sum of products of lengths
	 * @param avgQVol Average expected volume of a query box
	 * @param ignoreChecks ignore checks?
	 */
	public void generateQuery3(double[] min, double[] max, 
			final double[] maxLen, double[] maxLenProduct, final double avgQVol,
			boolean ignoreChecks) {
		int dims = min.length;
		
		//query create cube
		double[] len = new double[min.length];
		int nTries = 0;
		double vol = 1;
		do {
			for (int d = 0; d < dims-1; d++) {
				//calculate the average required len 
				final double avgLen = Math.pow(avgQVol/vol, 1./(dims-d));
				//Can't be smaller than that if other lengths should be < maxLen
				final double minLen = avgQVol/(vol*maxLenProduct[d+1]);  
				double variationUp = maxLen[d]-avgLen; // MAX - AVG
				double variationDown = avgLen-minLen; // AVG - MIN
				//0.999 to avoid queries outside the data domain
				double variation = Math.min(variationUp, variationDown) * 0.999 * 0.5;  
				len[d] = avgLen + (R.nextDouble()*2-1.)*variation;
				if (len[d] > 1000 || Double.isNaN(len[d]) || len[d] <= 0) {
					System.out.println(Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
				}
				vol *= len[d];
				min[d] = globalMin[d] + R.nextDouble()*(maxLen[d]-len[d]);
				max[d] = min[d]+len[d];
				if (!ignoreChecks && min[d]+len[d]*0.9 >= globalMax[d]) {
					//drop bad rectangles 
					throw new RuntimeException("m/l/m=" + min[d] + "/" + len[d] + "/" + max[d] + 
							"; gm/gm=" + globalMin[d] + "/" + globalMax[d]);
				}
			}
			
			//create cuboid/box of desired size by dropping random length
			len[dims-1] = avgQVol/vol;  //now the new len creates a rectangle/box of SIZE.
			//System.out.println("XXX " + Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
			if (nTries++ > 3) {
				System.out.println(Arrays.toString(len) + " vol=" + (vol*len[dims-1]) + " aVol=" + avgQVol);
				throw new IllegalStateException("dims=" + dims + "  N=" + S.cfgNEntries);
			}
		} while (len[dims-1] >= maxLen[dims-1]); //drop bad rectangles
		
		//TODO
	//	shuffle(len);
		
		//create location
		for (int d = 0; d < dims; d++) {
			min[d] = globalMin[d] + R.nextDouble()*(maxLen[d]-len[d]);
			max[d] = min[d]+len[d];
			if (!ignoreChecks && min[d]+len[d]*0.9 >= globalMax[d]) {
				//drop bad rectangles 
				throw new RuntimeException("m/l/m=" + min[d] + "/" + len[d] + "/" + max[d] + 
						"; gm/gm=" + globalMin[d] + "/" + globalMax[d]);
			}
		}
	}

	private void shuffle(double[] da) {
		// Fisher-Yates shuffle
		for (int i = da.length - 1; i > 0; i--) {
			int index = R.nextInt(i + 1);
			double a = da[index];
			da[index] = da[i];
			da[i] = a;
		}
	}

	public double min(int d) {
		return globalMin[d];
	}

	public double max(int d) {
		return globalMax[d];
	}

	public double len(int d) {
		return globalMax[d] - globalMin[d];
	}

}
