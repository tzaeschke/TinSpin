/*
 * Copyright 2016-2017 Tilmann Zaeschke
 * 
 * This file is part of TinSpin.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinspin.wrappers;

import ch.ethz.globis.tinspin.TestStats;

import org.tinspin.index.PointDistanceFunction;
import org.tinspin.index.PointEntryDist;
import org.tinspin.index.QueryIteratorKNN;
import org.tinspin.index.covertree.CoverTree;
import org.tinspin.index.covertree.Point;

import ch.ethz.globis.tinspin.wrappers.Candidate;

/**
 * (Faster) CoverTree.
 *
 */
public class PointCTZ extends Candidate {
	
	private CoverTree<double[]> phc;
	private final int dims;
	private final int N;
	private double[] data;
	private QueryIteratorKNN<PointEntryDist<double[]>> itKnn;

	
	/**
	 * Setup of a native CoverTree
	 * 
	 * @param ts test stats
	 */
	public PointCTZ(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void load(double[] data, int dims) {
		int pos = 0;
		//phc = CoverTree.create(dims);
		//double[][] allData = new double[N][dims];
		//ArrayList<Point<double[]>> list = new ArrayList<>(N);

		Point<double[]>[] points = new Point[N];
		for (int i = 0; i < N; i++) {
			double[] p = new double[dims];
			System.arraycopy(data, pos, p, 0, dims);
			pos += dims;
			Point<double[]> pp = CoverTree.create(p, p);
			points[i] = pp;
		}
		phc = CoverTree.create(points, 1.3, PointDistanceFunction.L2);

		
//		for (int n = 0; n < N; n++) {
//			//double[] p = allData[n];
//			double[] p = new double[dims];
//			System.arraycopy(data, pos, p, 0, dims);
//			pos += dims;
//			phc.insert(p, null);
////			Point<double[]> pp = new Point<>(p);
////			list.add(pp);
//		}
//		phc.load(list);
//		phc = CoverTree.create(list);
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
//		int n = 0;
//		double[] l = new double[dims];
//		for (int i = 0; i < N>>1; i++) {
//			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
//			n += phc.remove(getEntry(l, N-i-1)) != null ? 1 : 0;
//		}
//		if ((N%2) != 0) {
//			int i = (N>>1);
//			n += phc.remove(getEntry(l, i)) != null ? 1 : 0;
//		}
//		return n;
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int query(double[] min, double[] max) {
//		if (it == null) {
//			it = phc.query(min, max);
//		} else {
//			it.reset(min, max);
//		}
//		int n = 0;
//		while (it.hasNext()) {
//			it.next();
//			n++;
//		}
////		int n = ((PhTree7)phc).queryAll(min2, max2).size();
//		//log("q=" + Arrays.toString(q));
//		return n;
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		if (k == 1) {
			return phc.query1NN(center).dist();
		}
		if (itKnn == null) {
			itKnn = phc.queryKNN(center, k);
		} else {
			itKnn.reset(center, k);
		}
		double ret = 0;
		int n = 0;
		while (itKnn.hasNext() && n++ < k) {
			ret += itKnn.next().dist();
		}
		if (n < k) {
			throw new IllegalStateException("n/k=" + n + "/" + k);
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

	
	/**
	 * Used to test the native code during development process
	 */
	@Override
	public CoverTree<double[]> getNative() {
		return phc;
	}

	@Override
	public String getNativeStats() {
		return phc.getStats().toString();
	}

	@Override
	public void getStats(TestStats s) {
		CoverTree.CTStats stats = phc.getStats();
		s.statNnodes = stats.getNodeCount();
		s.statNpostlen = stats.getMaxDepth();
		s.statNDistCalc = stats.getNDistCalc();
		s.statNDistCalc1NN = stats.getNDistCalc1NN();
		s.statNDistCalcKNN = stats.getNDistCalcKNN();
		//phc.printStats(N);
		//phc.printQuality();
		//PhTreeStats q = phc.getStats();
		//S.setStats(q);
		//System.out.println(phc.getQuality());
	}
	
	@Override
	public int update(double[][] updateTable) {
//		int n = 0;
//		for (int i = 0; i < updateTable.length; ) {
//			double[] p1 = updateTable[i++];
//			double[] p2 = Arrays.copyOf(updateTable[i++], dims);
//			if (phc.update(p1, p1, p2, p2) != null) {
//				n++;
//			}
//		}
//		return n;
		return -1;
	}
	
	@Override
	public boolean supportsWindowQuery() {
		return false;
	}

	@Override
	public boolean supportsUpdate() {
		return false;
	}

	@Override
	public boolean supportsUnload() {
		return false;
	}
	
	@Override
	public String toString() {
		return phc.toString(); 
	}
}
