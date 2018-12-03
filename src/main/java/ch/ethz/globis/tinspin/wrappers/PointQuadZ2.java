/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import java.util.Arrays;
import java.util.List;

import org.tinspin.index.PointEntry;
import org.tinspin.index.QueryIterator;
import org.tinspin.index.qthypercube2.QEntryDist;
import org.tinspin.index.qthypercube2.QuadTreeKD2;
import org.tinspin.index.qthypercube2.QuadTreeKD2.QStats;

import ch.ethz.globis.tinspin.TestStats;
/**
 * Quadtree with HypercubeNavigation (HC).
 * 
 * @author Tilmann Z�schke
 *
 */
public class PointQuadZ2 extends Candidate {
	
	private QuadTreeKD2<double[]> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private final int maxNodeSize = 10;
	
	/**
	 * Setup of a native PH tree
	 * 
	 * @param ts test stats
	 */
	public PointQuadZ2(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		//phc = QuadTreeKD.create(dims);
	}
	
	@Override
	public void load(double[] data, int dims) {
		double[] min = new double[dims];
		double[] max = new double[dims];
		Arrays.fill(min, Double.POSITIVE_INFINITY);
		Arrays.fill(max, Double.NEGATIVE_INFINITY);
		for (int i = 0; i < N; i+=dims) {
			for (int d = 0; d < dims; d++) {
				double x = data[i*dims+d];
				if (x > max[d]) {
					max[d] = x;
				}
				if (x < min[d]) {
					min[d] = x;
				}
			}
		}

		double[] center = new double[dims];
		double r = 0;
		for (int i = 0; i < dims; i++) {
			center[i] = (max[i]+min[i])/2;
			double d = max[i]-min[i];
			if (r < d) {
				r = d;
			}
		}
		
		int maxNodeSize = this.maxNodeSize;
		if (2 * dims > maxNodeSize) {
			maxNodeSize = 2*dims;
		}
		
		phc = QuadTreeKD2.create(dims, maxNodeSize, center, r);

		for (int i = 0; i < N; i++) {
			double[] buf = new double[dims];
			for (int d = 0; d < dims; d++) {
				buf[d] = data[i*dims+d]; 
			}
			phc.insert(buf, buf);
		}
		this.data = data;
	}

	@Override
	public Object preparePointQuery(double[][] q) {
		return q;
	}

	@Override
	public int pointQuery(Object qA) {
		int n = 0;
		for (double[] q: (double[][])qA) {
			if (phc.containsExact(q)) {
				n++;
			}
			//log("q=" + Arrays.toString(q));
		}
		return n;
	}

	@Override
	public int unload() {
		int n = 0;
		double[] l = new double[dims];
		for (int i = 0; i < N>>1; i++) {
			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
			n += phc.remove(getEntry(l, N-i-1)) != null ? 1 : 0;
		}
		if ((N%2) != 0) {
			int i = (N>>1);
			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
		}
		return n;
	}

	private double[] getEntry(double[] val, int pos) {
		for (int d = 0; d < dims; d++) {
			val[d] = data[pos*dims+d];
		}
		return val;
	}
	
	private QueryIterator<PointEntry<double[]>> pit;
	
	@Override
	public int query(double[] min, double[] max) {
		if (pit == null) {
			pit = phc.query(min, max);
		} else {
			pit.reset(min, max);
		}
		int n = 0;
		while (pit.hasNext()) {
			pit.next();
			n++;
		}
//		int n = ((PhTree7)phc).queryAll(min2, max2).size();
		//log("q=" + Arrays.toString(q));
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		List<QEntryDist<double[]>> nn = phc.knnQuery(center, k);
		double ret = 0;
		for (int i = 0; i < k; i++) {
			ret += nn.get(i).dist();
		}
		return ret;
	}

	@Override
	public boolean supportsKNN() {
		return dims <= 15;
	}
	
	@Override
	public void release() {
		data = null;
	}

	
	/**
	 * Used to test the native code during development process
	 */
	@Override
	public QuadTreeKD2<double[]> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats s) {
		QStats qs = phc.getStats();
		s.statNnodes = qs.getNodeCount();
		s.statNpostlen = qs.getMaxDepth();
		//just for debugging...
		s.statNNodeAHC = qs.getEntryCount();
		//phc.printStats(N);
		//phc.printQuality();
		//PhTreeStats q = phc.getStats();
		//S.setStats(q);
		System.out.println(qs);
	}
	
	@Override
	public int update(double[][] updateTable) {
		int n = 0;
		for (int i = 0; i < updateTable.length; ) {
			double[] p1 = updateTable[i++];
			double[] p2 = Arrays.copyOf(updateTable[i++], dims);
			if (phc.update(p1, p2) != null) {
				n++;
			}
		}
		return n;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
