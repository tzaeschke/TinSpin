/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.Arrays;
import java.util.Random;

import ch.ethz.globis.phtree.pre.ColumnType;
import ch.ethz.globis.tinspin.TestStats;

public abstract class AbstractTest {

	protected final Random R;
	protected int N;
	protected int DIM;
	protected final double param1;
	protected final double param2;
	protected String paramStr;
	protected final TestStats S;

	protected AbstractTest(Random R, TestStats S) {
		this.R = R;
		this.S = S;
		this.N = S.cfgNEntries;
		this.DIM = S.cfgNDims;
		this.param1 = S.param1;
		this.param2 = S.param2;
		this.paramStr = S.paramStr;
	}
	

	public final TestStats.TST getTestType() {
		return S.TEST;
	}

	/**
	 * 
	 * @return Maximum distance for each update().
	 */
	public abstract double maxUpdateDistance();

	public abstract void queryCuboid(int resultsPerQuery, double[] min, double[] max);
	public abstract double[] generate();
	public abstract double[][] generateUpdates(int n, double[] data, double[][] ups);
	
	public ColumnType[] getColumnTypes() {
		ColumnType[] types = new ColumnType[DIM];
		Arrays.fill(types, new ColumnType.DoubleColumn(15));
		return types;
	}
	
	public void log(String str) {
		System.out.println(str);
	}
	
	public TestStats getTestStats() {
		return S;
	}
}
