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

public abstract class TestRectangle extends AbstractTest {
	
	protected TestRectangle(Random R, TestStats S) {
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
			ups = new double[n*4][DIM]; //2 points, 2 versions
		}
		HashSet<Integer> idxSet = new HashSet<>(n);
		for (int i = 0; i < ups.length; ) {
			int pos = R.nextInt(getN());
			while (idxSet.contains(pos)) {
				pos = R.nextInt(getN());
			}
			idxSet.add(pos);
			ids[i >> 2] = pos;
			double[] lo1 = ups[i++];
			double[] up1 = ups[i++];
			double[] lo2 = ups[i++];
			double[] up2 = ups[i++];
			for (int d = 0; d < DIM; d++) {
				lo1[d] = data[pos*DIM*2+d];
				up1[d] = data[pos*DIM*2+DIM+d];
				//move different for each dimension
				//can be positive or negative
				//allow reshaping of rectangle (but up>lo). 
				double mvL = R.nextDouble()*2*maxD-maxD;
				lo2[d] = lo1[d] + mvL;
				do {
					double mvU = R.nextDouble()*2*maxD-maxD;
					up2[d] = up1[d] + mvU;
				} while (up2[d] <= lo2[d]);
				data[pos*DIM*2+d] = lo2[d];
				data[pos*DIM*2+DIM+d] = up2[d];
			}
		}
		return ups;
	}
}
