/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.wrappers;

import ch.ethz.globis.tinspin.IndexHandle;
import ch.ethz.globis.tinspin.TestInstances;
import ch.ethz.globis.tinspin.TestStats;
import org.tinspin.index.Index;
import org.tinspin.index.PointMap;
import org.tinspin.index.Stats;
import org.tinspin.index.kdtree.KDTree;
import org.tinspin.index.qthypercube.QuadTreeKD;
import org.tinspin.index.qthypercube2.QuadTreeKD2;
import org.tinspin.index.qtplain.QuadTreeKD0;
import org.tinspin.index.rtree.RTree;
import org.tinspin.index.util.PointMapWrapper;

import java.util.Arrays;

/**
 * KD-Tree.
 * 
 * @author Tilmann Zäschke
 *
 */
public class PointTinSpin extends Candidate {

	private PointMap<double[]> phc;
	private final int dims;
	private final IndexHandle indexHandle;
	private final int N;
	private double[] data;
	private Index.PointIteratorKnn<double[]> itKnn;
	private Index.PointIterator<double[]> pit;

	/**
	 * Setup of a native PH tree
	 *
	 * @param ts test stats
	 */
	public PointTinSpin(TestStats ts) {
		this.N = ts.cfgNEntries;
		this.dims = ts.cfgNDims;
		this.indexHandle = ts.INDEX;
	}
	
	@Override
	public void load(double[] data, int dims) {
		switch ((TestInstances.IDX)indexHandle) {
			// case CBZ: phc = CritBit.create(dims, 10); break;
			// case CTZ: phc = CoverTree.create(dims); break;  // -> in separate class because it doesn't support all operations // TODO use sub-class?
			case KDZ: phc = KDTree.create(dims); break;
			case QT0Z: phc = QuadTreeKD0.create(dims, 10); break;
			case QTZ: phc = QuadTreeKD.create(dims, 10); break;
			case QT2Z: phc = QuadTreeKD2.create(dims, 10); break;
			case RSZ:
			case STRZ: phc = PointMapWrapper.create(RTree.createRStar(dims)); break;
			default: throw new UnsupportedOperationException("Not supported: " + indexHandle.name());
		}

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
	public int pointQuery(Object qA, int[] ids) {
		int n = 0;
		for (double[] q: (double[][])qA) {
			if (phc.queryExact(q) != null) {
				n++;
			}
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
		return n;
	}
	
	@Override
	public double knnQuery(int k, double[] center) {
		if (k == 1) {
			return phc.query1nn(center).dist();
		}
		if (itKnn == null) {
			itKnn = phc.queryKnn(center, k);
		} else {
			itKnn.reset(center, k);
		}
		double ret = 0;
		while (itKnn.hasNext()) {
			ret += itKnn.next().dist();
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
	public PointMap<double[]> getNative() {
		return phc;
	}

	@Override
	public void getStats(TestStats s) {
		Stats qs = phc.getStats();
		s.statNnodes = qs.getNodeCount();
		s.statNpostlen = qs.getMaxDepth();
		s.statNDistCalc = qs.getNDistCalc();
		s.statNDistCalc1NN = qs.getNDistCalc1NN();
		s.statNDistCalcKNN = qs.getNDistCalcKNN();
	}
	
	@Override
	public int update(double[][] updateTable, int[] ids) {
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
