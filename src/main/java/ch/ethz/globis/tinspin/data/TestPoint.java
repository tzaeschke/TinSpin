/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.util.HashSet;
import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;


public abstract class TestPoint extends AbstractTest {
	
	
	protected TestPoint(Random R, TestStats S) {
		super(R, S);
	}

	@Deprecated
	@Override
	public final double[][] generateUpdates(int n, double[] data, double[][] ups) {
		return generateUpdates(n, data, ups, new int[n]);
	}

	@Override
	public final double[][] generateUpdates(int n, double[] data, double[][] ups, int[] ids) {
		double maxD = maxUpdateDistance();
		if (ups == null) {
			ups = new double[n*2][DIM]; //2 points, 2 versions
		}
		HashSet<Integer> idxSet = new HashSet<>(n);
		for (int i = 0; i < ups.length; ) {
			int pos = R.nextInt(getN());
			while (idxSet.contains(pos)) {
				pos = R.nextInt(getN());
			}
			idxSet.add(pos);
			ids[i >> 1] = pos;
			double[] pOld = ups[i++];
			double[] pNew = ups[i++];
			for (int d = 0; d < DIM; d++) {
				pOld[d] = data[pos*DIM+d];
				//move different for each dimension
				//can be positive or negative
				//allow reshaping of rectangle (but up>lo). 
				double mvL = R.nextDouble()*2*maxD-maxD;
				pNew[d] = pOld[d] + mvL;
				data[pos*DIM+d] = pNew[d];
			}
		}
		return ups;
	}
}
