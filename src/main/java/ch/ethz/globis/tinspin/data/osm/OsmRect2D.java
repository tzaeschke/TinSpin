/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data.osm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.zoodb.internal.util.PrimLongMapLI;
import org.zoodb.jdo.ZooJdoProperties;
import org.zoodb.tools.ZooHelper;

import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTreeSolidF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhEntrySF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhIteratorSF;
import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.phtree.util.Tools;
import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.tiger.PersistentArrayDouble;
import ch.ethz.globis.tinspin.data.tiger.PersistentArrayDoubleParent;

public class OsmRect2D {

	private static final SimpleDateFormat FT = 
		      new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'");

	private double[] min, max;

	public static String OSM_PATH = "F:\\data\\OSM";
	
	private static final Object DUMMY = new Object();
	
	private static final int DIM = 2;
	
	public OsmRect2D() {
		min = new double[DIM];
		max = new double[DIM];
		Arrays.fill(min, Double.POSITIVE_INFINITY);
		Arrays.fill(max, Double.NEGATIVE_INFINITY);
	}

	public double[] min() {
		return min;
	}
	
	public double[] max() {
		return max;
	}
	
	public double[] readAndBuffer(String dbName, TestStats ts) {
		if (ts.cfgNDims != DIM) {
			throw new IllegalArgumentException();
		}
		PhTreeSolidF<Object> idx = null;

		ZooHelper.removeDb(dbName); //TODO 
		if (!ZooHelper.dbExists(dbName)) {
			//read from file
			System.out.println("Creating buffer file for OSM data: " + dbName);
			if (!ts.isRangeData) {
				throw new IllegalArgumentException();
			} else {
				int nPoints = ts.cfgNEntries * 2;//two point per segment, max
				double[][] data = new double[nPoints][ts.cfgNDims];
				long[] ids = new long[nPoints];
				readFolderPoints(OSM_PATH, data, ids);
				System.out.println("Loading point map...");
				PrimLongMapLI<double[]> map = new PrimLongMapLI<>(nPoints);
				for (int i = 0; i < data.length; i++) {
					map.put(ids[i], data[i]);
				}
				data = null;
				ids = null;
				
				idx = PhTreeSolidF.create(DIM);
				readFolderWays(OSM_PATH, map, idx, ts.cfgNEntries);
				ts.cfgNEntries = idx.size();
			}
			System.out.println("min/max longitude= " + min[0] + "/" + max[0] + 
					"   latitude=" + min[1] + "/" + max[1]);
			storeToDB(idx, dbName);
		} 

		//read from DB
		//We always read, because the original data[] may contain duplicates 
		System.out.println("Reading OSM data from buffer file: " + dbName);
		double[] data = readFromDB_Array(dbName, ts);
		
		for (int i = 0; i < data.length; i += 2) {
			minMax(data[i], 0);
			minMax(data[i+1], 1);
		}
		
//		if (false) {
//			//draw output
//			TestDraw.draw(data, DIM, 1, 2);
//		}
		return data;
	}

	private PhTree<Object> buildIndexPHT(double[] data, TestStats ts) {
		log("Building index");
		Object O = new Object();
		int dims = ts.cfgNDims;
		int MAX_E = ts.cfgNEntries;
		int N = data.length/(dims);
        long memTree = Tools.getMemUsed();
		long t1 = System.currentTimeMillis();
		PhTree<Object> ind = PhTree.create(dims);
		long[] l = new long[dims];
		int nDupl = 0;
		int n = 0;
		for (int i = 0; i < data.length; ) {
			for (int d = 0; d < dims; d++) {
				minMax( data[i], d );
				l[d] = BitTools.toSortableLong(data[i++]);
			}
			if (ind.put(l, O) != null) {
				nDupl++;
			} else {
				n++;
			}
			if (n%100000 == 0) {
				System.out.print('.');
				if (n%10000000 == 0) {
					System.out.println();
				}
			}
			if (n >= MAX_E) {
				break;
			}
		}
		long t2 = System.currentTimeMillis();
		ts.cfgNEntries = n;
		System.out.println("Duplicates: " + nDupl);
		System.out.println("Entries (max): " + (N-nDupl));
		System.out.println("Entries (done): " + n);
		System.out.println("Build-index time: " + (t2-t1)/1000.);
		Tools.cleanMem(n, memTree);
		return ind;
	}
	

	static void storeToDB(ArrayList<OSMEntry> entries, String dbName) {
		log("Storing to database");
		ZooHelper.getDataStoreManager().createDb(dbName);
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		//pmf.setRetainValues(true);  //TODO?
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		long t1 = System.currentTimeMillis();
		
		int n = 0;
		for (OSMEntry e: entries) {
			if (e == null) {
				continue;
			}
			pm.makePersistent(e);
			if (n++ % 10000 ==0) {
				long t2 = System.currentTimeMillis();
				System.out.println("n=" + n + "     t=" + (t2-t1));
				t1 = t2;
				pm.currentTransaction().commit();
				pm.currentTransaction().begin();
			}
		}
		
		pm.currentTransaction().commit();
		pm.close();
		pmf.close();
	}
	
	static void storeToDB(PhTreeSolidF<Object> idx, String dbName) {
		log("Storing to database");
		ZooHelper.getDataStoreManager().createDb(dbName);
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		pmf.setRetainValues(true);
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		//use size*2 for storing two vectors per rectangle
		PersistentArrayDoubleParent list = 
				new PersistentArrayDoubleParent(idx.size(), DIM*2);
		pm.makePersistent(list);
		
		int n = 0;
		int pos = 0;
		PhIteratorSF<Object> it = idx.iterator();
		double[] data = list.getNextForWrite().getData();
		while (it.hasNext()) {
			PhEntrySF<?> e = it.nextEntryReuse();
			for (int k = 0; k < DIM; k++) {
				data[pos++] = e.lower()[k]; 
			}
			for (int k = 0; k < DIM; k++) {
				data[pos++] = e.upper()[k]; 
			}
			
			if (++n % 100000 == 0) {
				System.out.print(".");
			}
			if (n % PersistentArrayDoubleParent.CHUNK_SIZE == 0) {
				pm.currentTransaction().commit();
				pm.currentTransaction().begin();
				data = list.getNextForWrite().getData();
				pos = 0;
			}
		}
		System.out.println();
		
		log("comitting...");
		pm.currentTransaction().commit();
		log("done");
		pm.close();
		pmf.close();
	}
	
	private static double min(double d1, double d2) {
		return d1<d2 ? d1 : d2;
	}
	private static double max(double d1, double d2) {
		return d1>d2 ? d1 : d2;
	}
	
	void minMax(double x, int d) {
		min[d] = min(x, min[d]);
		max[d] = max(x, max[d]);
	}
	
	static double[] readFromDB_Array(String dbName, TestStats ts) {
		log("Reading from database");
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		//pmf.setRetainValues(false);
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		Extent<PersistentArrayDoubleParent> ext = pm.getExtent(PersistentArrayDoubleParent.class);
		PersistentArrayDoubleParent pa = ext.iterator().next();
		ext.closeAll();
		
		int maxEntries;
		if (pa.getEntryCount() < ts.cfgNEntries) {
			maxEntries = pa.getEntryCount();
			ts.cfgNEntries = maxEntries;
		} else {
			maxEntries = ts.cfgNEntries;
		}
		
		double[] ret = new double[maxEntries*2*DIM];
		int pos = 0;
		for (PersistentArrayDouble d: pa.getData()) {
			double[] da = d.getData();
			if (pos+da.length <= ret.length) {
				System.arraycopy(da, 0, ret, pos, da.length);
			} else {
				//special case for last 'da' if we do not read all data
				if (ret.length-pos > 0) {
					System.arraycopy(da, 0, ret, pos, ret.length-pos);
				}
			}
			pos += da.length;
		}
		
		pm.currentTransaction().commit();
		pm.close();
		pmf.close();
		
		return ret;
	}
	
	
	private void readFolderPoints(String pathName, double[][] data, long[] ids) {
		int MAX_E = ids.length;
		File dir = new File(pathName);
		if (!dir.exists()) {
			return;
		}
		int pos = 0;
		for (File f: dir.listFiles()) {
			log("Reading file: " + f.getName());

			if (!f.getName().endsWith(".osm")) {
				//skip
				System.out.println("File skipped !!!!!!!!!!!!!!");
				continue;
			}

			pos = readFile_P(f, data, ids, pos, MAX_E);

			if (pos >= MAX_E) {
				break;
			}
		}
		//TODO do we need this?
		//data = Arrays.copyOf(data, pos);
		System.out.println("RF: Points: " + pos);
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private final int readFile_P(File fFile, double[][] data, long[] ids, 
			int pos, int MAX_E) {
		//Note that FileReader is used, not File, since File is not Closeable
		BufferedReader scanner;
		try {
			scanner = new BufferedReader(new FileReader(fFile));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		String nl = null;
		try {
			int nNode = 0;
			int nL = 0;
			String line = null;
			while ( (line = scanner.readLine()) != null ) {//hasNext()) {
				line = line.trim();
				nL++;
				//System.out.println(line);
				line = line.trim();
				if (line.startsWith("<node")) {
					nNode++;
					readOSM_P(line, data, ids, pos);
					pos++;
					if (nNode >= MAX_E) {
						return pos;
					}
					if (nNode % 10000 == 0) {
						System.out.print('.');
						if (nNode % 1000000 == 0) {
							System.out.println(" points:" + nNode);
						}
					}
				}
			}
			
			System.out.println("Nodes: " + nNode);
			System.out.println("Points: " + pos);
			System.out.println("Lines: " + nL);
		} catch (NumberFormatException e) {
			System.err.println("File: " + fFile.getAbsolutePath());
			System.err.println("Line: " + nl);
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			//ensure the underlying stream is always closed
			//this only has any effect if the item passed to the Scanner
			//constructor implements Closeable (which it does in this case).
			try {
				scanner.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return pos;
	}

	private static void readOSM_P(String line, double[][] node, long[] ids, int pos) {
		long id = -1;
		double lat = 0;
		double lon = 0;
		String user;
		int uid = -1;
		boolean visible = true;
		int version = -1;
		int changeset = -1;
		long date = -1;
		
		int k1 = 0;
		int k2 = -1;
		
		do {
			k1 = line.indexOf(' ', k2+1);
			if (k1 < 0) {
				break;
			}
			k2 = line.indexOf('=', k1+1);
			String key = line.substring(k1+1, k2);

			k1 = line.indexOf('"', k2+1);
			k2 = line.indexOf('"', k1+1);
			String val = line.substring(k1+1, k2);
			//System.out.println("key=" + key + "  val="+val);
			switch (key.charAt(0)) {
			case 'i': //if ("id".equals(key)) {
				id = Long.parseLong(val); break;
			case 'l': 
				if ("lon".equals(key)) {
					lon = Double.parseDouble(val);
				} else if ("lat".equals(key)) {
					lat = Double.parseDouble(val);
				} 
				break;
			case 'u': 
//				if ("user".equals(key)) {
//					user = val;
//				} else if ("uid".equals(key)) {
//					uid = Integer.parseInt(val);
//				}
				break;
			case 'v':
//				if ("visible".equals(key)) {
//					visible = Boolean.parseBoolean(val);
//				} else if ("version".equals(key)) {
//					version = Integer.parseInt(val);
//				}
				break;
			case 'c': //if ("changeset".equals(key)) {
//				changeset = Integer.parseInt(val); 
				break;
			case 't': //if ("timestamp".equals(key)) {
//				try {
//					date = FT.parse(val).getTime();
//				} catch (ParseException e) {
//					throw new RuntimeException(e);
//				}
				break;
			default:
				throw new IllegalStateException("key=" + key);
			}
		} while (true);
		
//		lat = Double.parseDouble(line.substring(k1+1, k2));
//
//		String user = line.substring(k1+1, k2);
//
//		uid = Integer.parseInt(line.substring(k1+1, k2));
//
//		visible = Boolean.parseBoolean(line.substring(k1+1, k2));
//
//		version = Integer.parseInt(line.substring(k1+1, k2));
//
//		changeset = Integer.parseInt(line.substring(k1+1, k2));
//
//		date = Date.parse(line.substring(k1+1, k2));

		node[pos][0] = lon;
		node[pos][1] = lat;
		ids[pos] = id;
//		node[pos+0] = id;
//		node[pos+1] = (long) (lon*10000000);
//		node[pos+2] = (long) (lat*10000000);
//		node[pos+3] = uid;
//		node[pos+4] = version;
//		node[pos+5] = changeset;
//		node[pos+6] = date;
		//node[pos+4] = visible ? 1 : 0;
	}

	private void readFolderWays(String pathName, 
			PrimLongMapLI<double[]> map,
			PhTreeSolidF<Object> idx,
			int MAX_E) {
		File dir = new File(pathName);
		if (!dir.exists()) {
			return;
		}
		int pos = 0;
		for (File f: dir.listFiles()) {
			log("Reading file: " + f.getName());

			if (!f.getName().endsWith(".osm")) {
				//skip
				System.out.println("File skipped !!!!!!!!!!!!!!");
				continue;
			}

			pos = readFile_R(f, map, idx, pos, MAX_E);

			if (pos >= MAX_E) {
				break;
			}
		}

		System.out.println("RF: Rectangles: " + pos);
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param idx 
	 * @param b2 
	 * @param entries2 
	 */
	private final int readFile_R(File fFile, PrimLongMapLI<double[]> map,
			PhTreeSolidF<Object> idx, int pos, int MAX_E) {
		//Note that FileReader is used, not File, since File is not Closeable
		BufferedReader scanner;
		try {
			scanner = new BufferedReader(new FileReader(fFile));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		String nl = null;
		try {
			int nRect = 0;
			int nL = 0;
			int nNotFound = 0;
			int nDuplicates = 0;
			String line = null;
			double[] prevNode = null;
			final double[] lo = new double[DIM];
			final double[] hi = new double[DIM];
			while ( (line = scanner.readLine()) != null ) {//hasNext()) {
				line = line.trim();
				nL++;
				//System.out.println(line);
				line = line.trim();
				if (line.startsWith("<nd")) {
					long id = readOSM_R(line);
					double[] node = map.get(id);
					if (node == null) {
						System.out.println("Node not found: " + id);
						nNotFound++;
						prevNode = null;
						continue;
					}
					if (prevNode != null) {
//						dataR[pos*4+0] = min(node[0], prevNode[0]);
//						dataR[pos*4+1] = min(node[1], prevNode[1]);
//						dataR[pos*4+2] = max(node[0], prevNode[0]);
//						dataR[pos*4+3] = max(node[1], prevNode[1]);
						lo[0] = min(node[0], prevNode[0]);
						lo[1] = min(node[1], prevNode[1]);
						hi[0] = max(node[0], prevNode[0]);
						hi[1] = max(node[1], prevNode[1]);
						if (idx.put(lo, hi, DUMMY) != null) {
							nDuplicates++;
						}
						nRect++;
					}
					prevNode = node;
					
					pos++;
					if (nRect >= MAX_E) {
						System.out.println("Rects: " + nRect);
						System.out.println("Points: " + pos);
						System.out.println("Lines: " + nL);
						System.out.println("Missing nodes: " + nNotFound);
						System.out.println("Duplicates: " + nDuplicates);
						return pos;
					}
					if (nRect % 10000 == 0) {
						System.out.print('.');
						if (nRect % 1000000 == 0) {
							System.out.println(" rects:" + nRect);
						}
					}
				} else { 
					prevNode = null;
				}
			}
			
			System.out.println("Rects: " + nRect);
			System.out.println("Points: " + pos);
			System.out.println("Lines: " + nL);
			System.out.println("Missing nodes: " + nNotFound);
			System.out.println("Duplicates: " + nDuplicates);
		} catch (NumberFormatException e) {
			System.err.println("File: " + fFile.getAbsolutePath());
			System.err.println("Line: " + nl);
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			//ensure the underlying stream is always closed
			//this only has any effect if the item passed to the Scanner
			//constructor implements Closeable (which it does in this case).
			try {
				scanner.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return pos;
	}

	private long readOSM_R(String line) {
		long id = -1;
		
		int k1 = 0;
		int k2 = -1;
		
//		do {
			k1 = line.indexOf(' ', k2+1);
//			if (k1 < 0) {
//				break;
//			}
			k2 = line.indexOf('=', k1+1);
//			String key = line.substring(k1+1, k2);

			k1 = line.indexOf('"', k2+1);
			k2 = line.indexOf('"', k1+1);
			String val = line.substring(k1+1, k2);
			//System.out.println("key=" + key + "  val="+val);
//			if ("ref".equals(key)) {
				id = Long.parseLong(val);
//			}
//		} while (true);
		
		return id;
	}

	private static void log(Object aObject){
		System.out.println(String.valueOf(aObject));
	}

}
