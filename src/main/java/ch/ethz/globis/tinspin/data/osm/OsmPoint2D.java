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

import org.zoodb.jdo.ZooJdoProperties;
import org.zoodb.tools.ZooHelper;

import ch.ethz.globis.phtree.PhTreeF;
import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.db.DbWriter;
import ch.ethz.globis.tinspin.db.PersistentArrayDouble;
import ch.ethz.globis.tinspin.db.PersistentArrayDoubleParent;

/**
 * OSM XML point reader.
 * 
 * This class reads all OSM XML files in the specified folder and
 * extracts all points (nodes). The nodes are then stored 
 * in a ZooDB for faster read.
 * Geometric duplicates are removed (i.e. points with identical 
 * coordinates).
 * 
 * @author Tilmann Zäschke
 *
 */
public class OsmPoint2D {

	@SuppressWarnings("unused")
	private static final SimpleDateFormat FT = 
		      new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'");

	private double[] min, max;

	public static String OSM_PATH = "F:\\data\\OSM";
	
	public OsmPoint2D() {
		min = new double[2];
		max = new double[2];
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
		if (ts.cfgNDims != 2) {
			throw new IllegalArgumentException();
		}

		//N=215981638
		//min/max longitude= 3.931094/20.2583918   latitude=37.7126446/49.1369103

		if (!ZooHelper.dbExists(dbName)) {
			//read from file
			System.out.println("Creating buffer file for OSM data: " + dbName);
			if (ts.isRangeData) {
				throw new UnsupportedOperationException();
			}
			
			readFolder(OSM_PATH, dbName, ts);
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
		System.out.println("N=" + data.length/2 + " / " + ts.cfgNEntries);
//		if (false) {
//			//draw output
//			TestDraw.draw(data, 100, MODE.POINTS);
//		}
		return data;
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
		
		double[] ret = new double[maxEntries*2];
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
	
	
	private void readFolder(String pathName, String dbName, TestStats ts) {
		File dir = new File(pathName);
		if (!dir.exists()) {
			return;
		}
		
		int MAX_E = ts.cfgNEntries;
		DbWriter w = new DbWriter(dbName);
		w.init(ts.cfgNDims);
		PhTreeF<Object> idxF = PhTreeF.create(ts.cfgNDims);
		
		int n = 0;
		for (File f: dir.listFiles()) {
			log("Reading file: " + f.getName());

			if (!f.getName().endsWith(".osm")) {
				//skip
				System.out.println("File skipped !!!!!!!!!!!!!!");
				continue;
			}

			n = readFile(f, n, MAX_E, idxF, w);

			if (n >= MAX_E) {
				break;
			}
		}
		w.close();
		System.out.println("RF: Points: " + n);
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param idxF 
	 * @param w 
	 * @param b2 
	 * @param entries2 
	 */
	private int readFile(File fFile, int nTotal, int MAX_E, 
			PhTreeF<Object> idxF, DbWriter w) {
		Object DUMMY = new Object();
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
			int nDupl = 0;
			String line = null;
			double[] node = new double[idxF.getDim()];
			while ( (line = scanner.readLine()) != null ) {//hasNext()) {
				line = line.trim();
				nL++;
				//System.out.println(line);
				line = line.trim();
				if (line.startsWith("<node")) {
					nNode++;
					readOSM(line, node);
					if (idxF.put(node, DUMMY) == null) {
						nTotal++;
						w.write(node);
						if (nTotal >= MAX_E) {
							return nTotal;
						}
					} else {
						nDupl++;
					}
				} else if (line.startsWith("<way")) {
					//abort, node block is finished.
					break;
				}
			}
			
			System.out.println("Nodes: " + nNode);
			System.out.println("Nodes total: " + nTotal);
			System.out.println("Nodes duplicates: " + nDupl);
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
		return nTotal;
	}

	private void readOSM(String line, double[] node) {
//		long id = -1;
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
			String val = line.substring(k1+1, k2);
			//System.out.println("key=" + key + "  val="+val);
			switch (key.charAt(0)) {
			case 'i': //if ("id".equals(key)) {
//				id = Long.parseLong(val); 
				break;
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

		node[0] = lon;
		node[1] = lat;
//		node[pos+0] = id;
//		node[pos+1] = (long) (lon*10000000);
//		node[pos+2] = (long) (lat*10000000);
//		node[pos+3] = uid;
//		node[pos+4] = version;
//		node[pos+5] = changeset;
//		node[pos+6] = date;
		//node[pos+4] = visible ? 1 : 0;
	}

	private static void log(Object aObject){
		System.out.println(String.valueOf(aObject));
	}

}
