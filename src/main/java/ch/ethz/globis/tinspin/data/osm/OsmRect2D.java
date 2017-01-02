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
import java.text.SimpleDateFormat;
import java.util.Arrays;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.zoodb.internal.util.PrimLongMapLI;
import org.zoodb.jdo.ZooJdoProperties;
import org.zoodb.tools.ZooHelper;

import ch.ethz.globis.phtree.PhTreeSolidF;
import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.db.DbWriter;
import ch.ethz.globis.tinspin.db.PersistentArrayDouble;
import ch.ethz.globis.tinspin.db.PersistentArrayDoubleParent;

/**
 * OSM XML rectangle (line segment) reader.
 * 
 * This class reads all OSM XML files in the specified folder and
 * extracts all lines (ways). The line segments are stored as rectangles
 * (bounding boxes) in a ZooDB for faster read.
 * Geometric duplicates are removed (i.e. line segments with identical 
 * bounding boxes).
 * 
 * @author Tilmann Zäschke
 *
 */
public class OsmRect2D {

	@SuppressWarnings("unused")
	private static final SimpleDateFormat FT = 
		      new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'");

	private double[] min, max;

	public static String OSM_PATH = "F:\\data\\OSM";
	
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

		if (!ZooHelper.dbExists(dbName)) {
			//read from file
			System.out.println("Creating buffer file for OSM data: " + dbName);
			if (!ts.isRangeData) {
				throw new IllegalArgumentException();
			} 

			System.out.println("Allocating arrays ...");
			//int nPoints = ts.cfgNEntries * 2;//two point per segment, max
			int nPoints = Integer.MAX_VALUE; //load all points
			
			System.out.println("Reading points ...");
			PrimLongMapLI<double[]> map = new PrimLongMapLI<>(nPoints);
			readFolderPoints(OSM_PATH, map, nPoints);
			
			System.out.println("Reading ways ...");
			readFolderWays(OSM_PATH, map, dbName, ts);
			map = null;
		} 

		//read from DB
		//We always read, because the original data[] may contain duplicates 
		System.out.println("Reading OSM data from buffer file: " + dbName);
		double[] data = readFromDB_Array(dbName, ts);
		
		for (int i = 0; i < data.length; i += 2) {
			minMax(data[i], 0);
			minMax(data[i+1], 1);
		}
		System.out.println("min/max longitude= " + min[0] + "/" + max[0] + 
				"   latitude=" + min[1] + "/" + max[1]);
		
//		if (false) {
//			//draw output
//			TestDraw.draw(data, DIM, 1, 2);
//		}
		return data;
	}

	private static double min(double d1, double d2) {
		return d1<d2 ? d1 : d2;
	}
	private static double max(double d1, double d2) {
		return d1>d2 ? d1 : d2;
	}
	
	private void minMax(double x, int d) {
		min[d] = min(x, min[d]);
		max[d] = max(x, max[d]);
	}
	
	private double[] readFromDB_Array(String dbName, TestStats ts) {
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
				break;
			}
			pos += da.length;
		}
		
		pm.currentTransaction().commit();
		pm.close();
		pmf.close();
		
		return ret;
	}
	
	
	private void readFolderPoints(String pathName, PrimLongMapLI<double[]> map, int MAX_E) {
		File dir = new File(pathName);
		if (!dir.exists()) {
			throw new IllegalArgumentException("OSM file not found: " + pathName);
		}
		int pos = 0;
		for (File f: dir.listFiles()) {
			log("Reading file: " + f.getName());

			if (!f.getName().endsWith(".osm")) {
				//skip
				System.out.println("File skipped !!!!!!!!!!!!!!");
				continue;
			}

			pos = readFile_P(f, map, pos, MAX_E);

			if (pos >= MAX_E) {
				break;
			}
		}
		System.out.println("RF: Points: " + pos);
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private final int readFile_P(File fFile, PrimLongMapLI<double[]> map, 
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
					readOSM_P(line, map);
					pos++;
					if (nNode >= MAX_E) {
						return pos;
					}
					printProgress(nNode);
				} else if (line.startsWith("<way")) {
					//abort, node block is finished.
					break;
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

	private void printProgress(int n) {
		if (n % 10000 == 0) {
			System.out.print('.');
			if (n % 1000000 == 0) {
				System.out.println(" " + n);
			}
		}
	}
	
	private void readOSM_P(String line, PrimLongMapLI<double[]> map) {
		long id = -1;
		double lat = 0;
		double lon = 0;
//		String user;
//		int uid = -1;
//		boolean visible = true;
//		int version = -1;
//		int changeset = -1;
//		long date = -1;
		
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
			//System.out.println("key=" + key + "  val="+val);
			//String val = line.substring(k1+1, k2);
			
			int c0 = key.charAt(0);
			if (c0 == 'i') {
				String val = line.substring(k1+1, k2);
				id = Long.parseLong(val); 
			} else if (c0 == 'l') {
				char c1 = key.charAt(1);
				char c2 = key.charAt(2);
				if (c1 == 'o' && c2 == 'n') { // 'lon' 
					String val = line.substring(k1+1, k2);
					lon = Double.parseDouble(val);
				} else if (c1 == 'a' && c2 == 't') { // 'lat'
					String val = line.substring(k1+1, k2);
					lat = Double.parseDouble(val);
				}
			}

//			switch (key.charAt(0)) {
//			case 'i': //if ("id".equals(key)) {
//				id = Long.parseLong(val); break;
//			case 'l': 
//				if ("lon".equals(key)) {
//					lon = Double.parseDouble(val);
//				} else if ("lat".equals(key)) {
//					lat = Double.parseDouble(val);
//				} 
//				break;
//			case 'u': 
//				if ("user".equals(key)) {
//					user = val;
//				} else if ("uid".equals(key)) {
//					uid = Integer.parseInt(val);
//				}
//				break;
//			case 'v':
//				if ("visible".equals(key)) {
//					visible = Boolean.parseBoolean(val);
//				} else if ("version".equals(key)) {
//					version = Integer.parseInt(val);
//				}
//				break;
//			case 'c': //if ("changeset".equals(key)) {
//				changeset = Integer.parseInt(val); 
//				break;
//			case 't': //if ("timestamp".equals(key)) {
//				try {
//					date = FT.parse(val).getTime();
//				} catch (ParseException e) {
//					throw new RuntimeException(e);
//				}
//				break;
//			default:
//				throw new IllegalStateException("key=" + key);
//			}
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

		double[] node = new double[DIM];
		node[0] = lon;
		node[1] = lat;
		map.put(id, node);
//		node[pos][0] = lon;
//		node[pos][1] = lat;
//		ids[pos] = id;
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
			PrimLongMapLI<double[]> map, String dbName, TestStats ts) {
		File dir = new File(pathName);
		if (!dir.exists()) {
			return;
		}
		
		int MAX_E = ts.cfgNEntries;
		DbWriter w = new DbWriter(dbName);
		w.init(ts.cfgNDims*2);
		PhTreeSolidF<Object> idxF = PhTreeSolidF.create(ts.cfgNDims);

		int n = 0;
		for (File f: dir.listFiles()) {
			log("Reading file: " + f.getName());

			if (!f.getName().endsWith(".osm")) {
				//skip
				System.out.println("File skipped !!!!!!!!!!!!!!");
				continue;
			}

			n = readFile_R(f, map, idxF, n, MAX_E, w);

			if (n >= MAX_E) {
				break;
			}
		}
		w.close();
		System.out.println("RF: Rectangles: " + n);
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param idx 
	 * @param b2 
	 * @param entries2 
	 */
	private final int readFile_R(File fFile, PrimLongMapLI<double[]> map,
			PhTreeSolidF<Object> idx, int nTotal, int MAX_E, 
			DbWriter w) {
		Object DUMMY = new Object();
		//Note that FileReader is used, not File, since File is not Closeable
		BufferedReader scanner;
		try {
			//TODO instead of going through the file again, we should
			//     continue using the scanner from the point reader.
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
				if (line.startsWith("<nd")) {
					long id = readOSM_R(line);
					double[] node = map.get(id);
					if (node == null) {
						System.out.println("Node not found: " + id);
						nNotFound++;
						prevNode = null;
						continue;
					}
					if (prevNode != null) {//TODO remove
//						dataR[pos*4+0] = min(node[0], prevNode[0]);
//						dataR[pos*4+1] = min(node[1], prevNode[1]);
//						dataR[pos*4+2] = max(node[0], prevNode[0]);
//						dataR[pos*4+3] = max(node[1], prevNode[1]);
						lo[0] = min(node[0], prevNode[0]);
						lo[1] = min(node[1], prevNode[1]);
						hi[0] = max(node[0], prevNode[0]);
						hi[1] = max(node[1], prevNode[1]);
						if (idx.put(lo, hi, DUMMY) == null) {
							nTotal++;
							w.write(lo, hi);
							if (nTotal >= MAX_E) {
								return nTotal;
							}
						} else {
							nDuplicates++;
						}
						nRect++;
					}
					prevNode = node;
					
					if (nTotal >= MAX_E) {
						System.out.println();
						System.out.println("Rects: " + nRect);
						System.out.println("Rects total: " + nTotal);
						System.out.println("Lines: " + nL);
						System.out.println("Missing nodes: " + nNotFound);
						System.out.println("Duplicates: " + nDuplicates);
						return nTotal;
					}
				} else { 
					prevNode = null;
					if (line.startsWith("<relation")) {
						//abort, 'ways' block is finished.
						break;
					}
				}
			}
			
			System.out.println();
			System.out.println("Rects: " + nRect);
			System.out.println("Rects total: " + nTotal);
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
		return nTotal;
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
