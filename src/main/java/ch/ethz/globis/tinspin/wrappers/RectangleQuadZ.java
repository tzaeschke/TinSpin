/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;
import java.util.List;

import org.zoodb.index.quadtree.QREntryDist;
import org.zoodb.index.quadtree.QuadTreeRKD;
import org.zoodb.index.quadtree.QuadTreeKD.QStats;
import org.zoodb.index.quadtree.QuadTreeRKD.QIterator;

import ch.ethz.globis.tinspin.TestStats;

public class RectangleQuadZ extends Candidate {
	
	private final QuadTreeRKD<Object> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private static final Object O = new Object();
	private QIterator<Object> query = null;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public RectangleQuadZ(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		this.phc = QuadTreeRKD.create(dims);
	}
	
	@Override
	public void load(double[] data, int dims) {
		int pos = 0;
		for (int n = 0; n < N; n++) {
			double[] lo = new double[dims];
			double[] hi = new double[dims];
			System.arraycopy(data, pos, lo, 0, dims);
			pos += dims;
			System.arraycopy(data, pos, hi, 0, dims);
			pos += dims;
			phc.put(lo, hi, O);
		}
		this.data = data;
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		//TODO
		phc.getStats();
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		return q;
	}

	@Override
	public int pointQuery(Object qA) {
		int n = 0;
		double[][] dA = (double[][]) qA; 
		for (int i = 0; i < dA.length; i+=2) {
			if (phc.containsExact(dA[i], dA[i+1])) {
				n++;
			}
		}
		return n;
	}

	@Override
	public boolean supportsPointQuery() {
		return false;
	}
	
	@Override
	public int unload() {
		int n = 0;
		double[] lo = new double[dims];
		double[] hi = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			System.arraycopy(data, i*dims*2, lo, 0, dims);
			System.arraycopy(data, i*dims*2+dims, hi, 0, dims);
			n += phc.removeExact(lo, hi) != null ? 1 : 0;
			int i2 = N-i-1;
			System.arraycopy(data, i2*dims*2, lo, 0, dims);
			System.arraycopy(data, i2*dims*2+dims, hi, 0, dims);
			n += phc.removeExact(lo, hi) != null? 1 : 0;
		}
		return n;
	}
	
	
	@Override
	public int query(double[] min, double[] max) {
		if (query == null) {
			query = phc.query(min, max);
		} else {
			query.reset(min, max);
		}
		int n = 0;
		while (query.hasNext()) {
			query.next();
			n++;
		}
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		List<QREntryDist<Object>> result = phc.knnSearch(center, k);
		double ret = 0;
		for (int i = 0; i < k; i++) {
			QREntryDist<Object> e = result.get(i);
			ret += e.getDistance();
		}
		return ret;
	}

	@Override
	public boolean supportsKNN() {
		return true;
	}
	
	@Override
	public void release() {
		data = null;
	}

	public QuadTreeRKD<Object> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats S) {
		QStats qs = phc.getStats();
		S.statNnodes = qs.getNodeCount(); 
		S.statNpostlen = qs.getMaxDepth();
	}
	


	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] lo1 = updateTable[i++];
			double[] up1 = updateTable[i++];
			double[] lo2 = Arrays.copyOf(updateTable[i++], dims);
			double[] up2 = Arrays.copyOf(updateTable[i++], dims);
			if (phc.update(lo1, up1, lo2, up2) != null) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public boolean supportsUpdate() {
		return true;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
