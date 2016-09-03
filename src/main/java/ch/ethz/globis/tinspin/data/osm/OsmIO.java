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
import java.util.Iterator;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.zoodb.jdo.ZooJdoProperties;
import org.zoodb.tools.ZooHelper;

import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTree.PhExtent;
import ch.ethz.globis.phtree.demo.OSM;
import ch.ethz.globis.phtree.nv.PhTreeNV;
import ch.ethz.globis.phtree.util.Tools;
import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.data.tiger.PersistentArrayDoubleParent;
import ch.ethz.globis.tinspin.data.tiger.PersistentArrayLong;
import ch.ethz.globis.tinspin.data.tiger.PersistentArrayLongParent;
import ch.ethz.globis.tinspin.data.tiger.TigerKmlIO;
import ch.ethz.globis.tinspin.util.TestDraw;

public class OsmIO {

	private static final SimpleDateFormat FT = 
		      new SimpleDateFormat ("yyyy-MM-dd'T'hh:mm:ss'Z'");

	private long[] min, max;

	public static String OSM_PATH = "D:\\data\\TigerKML";
	
	public OsmIO(int DIM) {
		min = new long[DIM];
		max = new long[DIM];
	}

	public double[] readAndBuffer(String dbName, TestStats ts) {
		PhTree<Object> idx = null;

		//DB_ARRAY --> DB_ARRAY_NO_DUP
		//Duplicates: 8685895
		//Entries: 10281158
		//Entries: 29248211
		
		//FILES --> DB_ARRAY_NO_DUP
		//Duplicates: 18875845
		//Entries: -62110
		//Entries: 18751626

		
		//n=18967053     t=88065
		//min/max: -178.977381/0.0  179.859681/71.441059

		long[] data;
		if (!ZooHelper.dbExists(dbName)) {
			//read from file
			System.out.println("Creating buffer file for OSM data: " + dbName);
			if (!ts.isRangeData) {
				data = readFolder(OSM_PATH, ts.cfgNDims, ts.cfgNEntries);
			} else {
				data = readFolderRectangle(OSM_PATH, ts.cfgNDims, Integer.MAX_VALUE);
			}
			idx = buildIndexPHT(data, ts); 
			System.out.println("min/max id= " + min[0] + "/"+ max[0] + 
					"   X=" + min[1] + "/" + max[1] +
					"   Y=" + min[2] + "/" + max[2]);
			storeToDB(idx, dbName);
		} else {
			//read from DB
			System.out.println("Reading OSM data from buffer file: " + dbName);
			data = readFromDB_Array(dbName);
		}
		
//		if (false) {
//			//draw output
//			TestDraw.draw(data, DIM, 1, 2);
//		}
		return data;
	}

	private PhTree<Object> buildIndexPHT(long[] data, TestStats ts) {
		log("Building index");
		Object O = new Object();
		int dims = ts.cfgNDims;
		int MAX_E = ts.cfgNEntries;
		int N = data.length/(dims);
        long memTree = Tools.getMemUsed();
		long t1 = System.currentTimeMillis();
		PhTree<Object> ind = PhTree.create(idxDim);
		long[] l = new long[idxDim];
		int nDupl = 0;
		int n = 0;
		for (int i = 0; i < data.length; ) {
			for (int d = 0; d < dims; d++) {
				minMax( data[i], d );
				l[d] = data[i++];
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
	

	static void storeToDB(ArrayList<OSMEntry> entries) {
		log("Storing to database");
		ZooHelper.getDataStoreManager().createDb(OSM.DB_NAME);
		
		ZooJdoProperties prop = new ZooJdoProperties(OSM.DB_NAME);
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
	
	static void storeToDB(PhTree<Object> idx, String dbName) {
		log("Storing to database");
		ZooHelper.getDataStoreManager().createDb(dbName);
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		pmf.setRetainValues(true);
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		PersistentArrayLongParent list = new PersistentArrayLongParent(idx.size(), idx.getDim());
		pm.makePersistent(list);
		
		int n = 0;
		int pos = 0;
		final int DIM = idx.getDim(); 
		PhExtent<Object> it = idx.queryExtent();
		long[] data = list.getNextForWrite().getData();
		while (it.hasNext()) {
			long[] v = it.nextEntryReuse().getKey();
			for (int k = 0; k < DIM; k++) {
				data[pos++] = v[k]; 
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
	
	private static long min(long d1, long d2) {
		return d1<d2 ? d1 : d2;
	}
	private static long max(long d1, long d2) {
		return d1>d2 ? d1 : d2;
	}
	
	void minMax(long x, int d) {
		min[d] = min(x, min[d]);
		max[d] = max(x, max[d]);
	}
	
	static long[] readFromDB_Array(String dbName) {
		log("Reading from database");
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		//pmf.setRetainValues(false);
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		Extent<PersistentArrayLongParent> ext = pm.getExtent(PersistentArrayLongParent.class);
		PersistentArrayLongParent pa = ext.iterator().next();
		ext.closeAll();
		
		long[] ret = new long[(int) (pa.getEntryCount() * pa.getDim())];
		int pos = 0;
		for (PersistentArrayLong d: pa.getData()) {
			long[] da = d.getData();
			System.arraycopy(da, 0, ret, pos, da.length);
			pos += da.length;
		}
		
		pm.currentTransaction().commit();
		pm.close();
		pmf.close();
		
		return ret;
	}
	
	
	static long[] readFolder(String pathName, int DIM, int MAX_E) {
		try {
			File dir = new File(pathName);
			if (!dir.exists()) {
				return null;
			}
			long[] data = new long[DIM*MAX_E];
			int pos = 0;
			for (File f: dir.listFiles()) {
				log("Reading file: " + f.getName());
				
				if (!f.getName().endsWith(".osm")) {
					//skip
					System.out.println("File skipped !!!!!!!!!!!!!!");
					continue;
				}
				
				pos = readFile(f, data, pos, DIM, MAX_E);

				if (pos >= MAX_E) {
					break;
				}
			}
			data = Arrays.copyOf(data, pos);
			System.out.println("RF: doubles: " + pos);
			System.out.println("RF: Points: " + pos/DIM);
			return data;
		} finally {

		}
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private static final int readFile(File fFile, long[] data, int pos, int DIM, int MAX_E) {
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
					readOSM(line, data, pos);
					pos+=DIM;
					if (nNode >= MAX_E) {
						return pos;
					}
					if (nNode % 10000 == 0) {
						System.out.print('.');
						if (nNode % 1000000 == 0) {
							System.out.println();
						}
					}
				}
			}
			
			System.out.println("Nodes: " + nNode);
			System.out.println("Points: " + pos/DIM);
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

	private static void readOSM(String line, long[] node, int pos) {
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
				if ("user".equals(key)) {
					user = val;
				} else if ("uid".equals(key)) {
					uid = Integer.parseInt(val);
				}
				break;
			case 'v':
				if ("visible".equals(key)) {
					visible = Boolean.parseBoolean(val);
				} else if ("version".equals(key)) {
					version = Integer.parseInt(val);
				}
				break;
			case 'c': //if ("changeset".equals(key)) {
				changeset = Integer.parseInt(val); break;
			case 't': //if ("timestamp".equals(key)) {
				try {
					date = FT.parse(val).getTime();
				} catch (ParseException e) {
					throw new RuntimeException(e);
				}
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

		node[pos+0] = id;
		node[pos+1] = (long) (lon*10000000);
		node[pos+2] = (long) (lat*10000000);
		node[pos+3] = uid;
		node[pos+4] = version;
		node[pos+5] = changeset;
		node[pos+6] = date;
		//node[pos+4] = visible ? 1 : 0;
	}

	private static void log(Object aObject){
		System.out.println(String.valueOf(aObject));
	}

}
