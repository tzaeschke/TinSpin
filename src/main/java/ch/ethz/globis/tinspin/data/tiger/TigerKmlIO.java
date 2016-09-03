/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data.tiger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.zoodb.jdo.ZooJdoProperties;
import org.zoodb.tools.ZooHelper;

import ch.ethz.globis.phtree.PhTree;
import ch.ethz.globis.phtree.PhTree.PhExtent;
import ch.ethz.globis.phtree.util.BitTools;
import ch.ethz.globis.phtree.util.Tools;
import ch.ethz.globis.tinspin.TestStats;

/**
 * Reads data from TIGER files and, if desired buffers them in a ZooDB file.
 * 
 * Data from Tiger/LINE: http://www2.census.gov/geo/tiger/KML/2010_Proto/2010tract_dt/
 * 
 * @author Tilmann Zäschke
 *
 */
public class TigerKmlIO {

	public static double minX = Double.MAX_VALUE;
	public static double maxX = -Double.MAX_VALUE;
	public static double minY = Double.MAX_VALUE;
	public static double maxY = -Double.MAX_VALUE;

	public static String TIGER_PATH = "D:\\data\\TigerKML";
	
	public static double[] readAndBuffer(String dbName, TestStats ts) {
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

		double[] data;
		if (!ZooHelper.dbExists(dbName)) {
			//read from file
			System.out.println("Creating buffer file for TIGER data: " + dbName);
			if (!ts.isRangeData) {
				data = TigerKmlIO.readFolder(TIGER_PATH, ts.cfgNEntries);
			} else {
				data = TigerKmlIO.readFolderRectangle(TIGER_PATH, ts.cfgNDims, ts.cfgNEntries);
			}
			idx = buildIndexPHT(data, ts, Integer.MAX_VALUE); 
			System.out.println("min/max X= " + TigerKmlIO.minX + "/"+ TigerKmlIO.maxX + "   Y=" + 
					TigerKmlIO.minY + "/" + TigerKmlIO.maxY);
			TigerKmlIO.storeToDB(idx, dbName);
		} else {
			//read from DB
			System.out.println("Reading TIGER data from buffer file: " + dbName);
			data = readFromDB_Array(dbName);
		}
		
//		if (false) {
//			//draw output
//			TestDraw.draw(data, 2);
//		}
		return data;
	}
	
	private static PhTree<Object> buildIndexPHT(double[] data, 
			TestStats ts, int MAX_ENTRIES) {
		log("Building index");
		int dims = ts.cfgNDims;
		int idxDims = dims * 2;
		int N = data.length/(dims);
        long memTree = Tools.getMemUsed();
		long t1 = System.currentTimeMillis();
		PhTree<Object> ind = PhTree.create(idxDims);
		Object OBJ = new Object();
		long[] l = new long[idxDims];
		int nDupl = 0;
		int n = 0;
		for (int i = 0; i < data.length; ) {
			l[0] = f2l(data[i++]);
			l[1] = f2l(data[i++]);
			TigerKmlIO.minMax( data[i-2], data[i-1] );
			if (ts.isRangeData) {
				l[2] = f2l(data[i++]);
				l[3] = f2l(data[i++]);
				TigerKmlIO.minMax( data[i-2], data[i-1] );
				if (l[0]>l[2]) {
					long d = l[0]; 
					l[0] = l[2];
					l[2] = d;
				}
				if (l[1]>l[3]) {
					long d = l[1]; 
					l[1] = l[3];
					l[3] = d;
				}
			}
			if (ind.put(l, OBJ) != null) {
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
			if (n >= MAX_ENTRIES) {
				break;
			}
		}
		long t2 = System.currentTimeMillis();
		ts.cfgNEntries = n;
		//ts.statTLoad = t2-t1;
		System.out.println("Duplicates: " + nDupl);
		System.out.println("Entries (max): " + (N-nDupl));
		System.out.println("Entries (done): " + n);
		System.out.println("Build-index time: " + (t2-t1)/1000.);
		Tools.cleanMem(n, memTree);
		return ind;
	}

	private static long f2l(double f) {
		return BitTools.toSortableLong(f);
	}

	private static double l2f(long l) {
		return BitTools.toDouble(l);
	}
	
	private static void storeToDB(PhTree<Object> idx, String dbName) {
		log("Storing to database");
		ZooHelper.getDataStoreManager().createDb(dbName);
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		pmf.setRetainValues(true);
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		PersistentArrayDoubleParent list = 
				new PersistentArrayDoubleParent(idx.size(), idx.getDim());
		pm.makePersistent(list);
		
		double[] data = list.getNextForWrite().getData();
		int pos = 0;
		
		int n = 0;
		final int dims = idx.getDim(); 
		PhExtent<Object> it = idx.queryExtent();
		while (it.hasNext()) {
			long[] v = it.nextEntryReuse().getKey();
			for (int k = 0; k < dims; k++) {
				data[pos++] = l2f(v[k]); 
			}
			
			if (++n % 100000 == 0) {
				System.out.print(".");
			}
			if (n % PersistentArrayDoubleParent.CHUNK_SIZE == 0) {
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
	
	public static void minMax(double x, double y) {
		minX = min(x, minX);
		maxX = max(x, maxX);
		minY = min(y, minY);
		maxY = max(y, maxY);
	}
	
	public static double[] readFromDB_Array(String dbName) {
		log("Reading from database");
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		pmf.setRetainValues(false);
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		Extent<PersistentArrayDoubleParent> ext = pm.getExtent(PersistentArrayDoubleParent.class);
		PersistentArrayDoubleParent pa = ext.iterator().next();
		ext.closeAll();
		
		double[] ret = new double[(int) (pa.getEntryCount() * pa.getDim())];
		int pos = 0;
		for (PersistentArrayDouble d: pa.getData()) {
			double[] da = d.getData();
			System.arraycopy(da, 0, ret, pos, da.length);
			pos += da.length;
		}
		
		pm.currentTransaction().commit();
		pm.close();
		pmf.close();
		
		return ret;
	}
	
	
	private static double[] readFolder(String pathName, int MAX_E) {
		try {
			File dir = new File(pathName);
			if (!dir.exists()) {
				return null;
			}
			double[] data = new double[100*1000*1000];
			int pos = 0;
			for (File f: dir.listFiles()) {
				log("Reading file: " + f.getName());
				
				if (f.getName().contains("_02.kml") 
						|| f.getName().contains("_15.kml")
						|| f.getName().contains("_72.kml")) {
					//skip
					//02 = Hawaii
					//15 = Alaska
					//72 = Cuba??
					System.out.println("File skipped !!!!!!!!!!!!!!");
					continue;
				}
				
				pos = readFile(f, data, pos, MAX_E);

				if (pos >= MAX_E) {
					break;
				}
			}
			data = Arrays.copyOf(data, pos);
			System.out.println("RF: doubles: " + pos);
			System.out.println("RF: Points: " + pos/2);
			return data;
		} finally {

		}
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private static final int readFile(File fFile, double[] data, int pos, int MAX_E) {
		//log("Loading...");
		//Note that FileReader is used, not File, since File is not Closeable
		Scanner scanner;
		try {
			scanner = new Scanner(new BufferedReader(new FileReader(fFile)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		//log("Header");
		String nl = null;
		boolean hasFailed = false;
		try {
			int nLinearRing = 0;
			while ( scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.equals("<LinearRing>")) {
					nLinearRing++;
					line = scanner.next();
					if (!line.startsWith("<coordinates>")) {
						throw new IllegalStateException();
					}
					if (line.length() == 13) {
						line = scanner.next();
					} else {
						line = line.substring(13);
					}
					
					while (!line.startsWith("<")) {
						readPoint(line, data, pos);
						line = scanner.next();
						if (data[pos] > 0 || data[pos] < -130 || data[pos+1] < 20) {
							hasFailed = true;
							continue;
						}
						pos+=2;
						
						if (pos/2 >= MAX_E) {
							return pos;
						}
					}
				}
			}
			
			System.out.println("LinearRing: " + nLinearRing);
			System.out.println("Points: " + pos/2);
			
		} catch (NumberFormatException e) {
			System.err.println("File: " + fFile.getAbsolutePath());
			System.err.println("Line: " + nl);
			throw new RuntimeException(e);
		} finally {
			//ensure the underlying stream is always closed
			//this only has any effect if the item passed to the Scanner
			//constructor implements Closeable (which it does in this case).
			scanner.close();
		}
		if (hasFailed) {
			System.out.println("FAILED !!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		return pos;
	}

	private static double[] readFolderRectangle(String pathName, int DIM, int MAX_E) {
		try {
			File dir = new File(pathName);
			if (!dir.exists()) {
				return null;
			}
			double[] data = new double[200*1000*1000];
			int pos = 0;
			for (File f: dir.listFiles()) {
				log("Reading file: " + f.getName());
				
				if (f.getName().contains("_02.kml") 
						|| f.getName().contains("_15.kml")
						|| f.getName().contains("_72.kml")) {
					//skip
					//02 = Hawaii
					//15 = Alaska
					//72 = Cuba??
					System.out.println("File skipped !!!!!!!!!!!!!!");
					continue;
				}
				
				pos = readFileRect(f, data, pos, DIM, MAX_E);

				if (pos >= MAX_E) {
					break;
				}
			}
			data = Arrays.copyOf(data, pos);
			System.out.println("RF: doubles: " + pos);
			System.out.println("RF: Rectangles: " + pos/4);
			return data;
		} finally {

		}
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private static final int readFileRect(File fFile, double[] data, int pos, int DIM, int MAX_E) {
		//log("Loading...");
		//Note that FileReader is used, not File, since File is not Closeable
		Scanner scanner;
		try {
			scanner = new Scanner(new BufferedReader(new FileReader(fFile)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		//log("Header");
		String nl = null;
		boolean hasFailed = false;
		try {
			int nLinearRing = 0;
			ArrayList<double[]> points = new ArrayList<>();
			while ( scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.equals("<LinearRing>")) {
					nLinearRing++;
					line = scanner.next();
					if (!line.startsWith("<coordinates>")) {
						throw new IllegalStateException();
					}
					if (line.length() == 13) {
						line = scanner.next();
					} else {
						line = line.substring(13);
					}

					points.clear();
					while (!line.startsWith("<")) {
						double[] p = new double[DIM];
						readPoint(line, p, 0);
						line = scanner.next();
						if (p[0] > 0 || p[0] < -130 || p[1] < 20) {
							hasFailed = true;
							continue;
						}
						points.add(p);
					}
					for (int i = 1; i < points.size(); i++) {
						double[] p1 = points.get(i-1);
						double[] p2 = points.get(i);
						data[pos+0] = p1[0];
						data[pos+1] = p1[1];
						data[pos+2] = p2[0];
						data[pos+3] = p2[1];
						if (data[pos  ] > data[pos+2]) {
							double t = data[pos  ];
							data[pos  ] = data[pos+2];
							data[pos+2] = t;
						}
						if (data[pos+1] > data[pos+3]) {
							double t = data[pos+1];
							data[pos+1] = data[pos+3];
							data[pos+3] = t;
						}
						pos+=4;
						
						if (pos/4 >= MAX_E) {
							return pos;
						}
					}
				}
			}
			
			System.out.println("LinearRing: " + nLinearRing);
			System.out.println("Rectangles: " + pos/4);
			
		} catch (NumberFormatException e) {
			System.err.println("File: " + fFile.getAbsolutePath());
			System.err.println("Line: " + nl);
			throw new RuntimeException(e);
		} finally {
			//ensure the underlying stream is always closed
			//this only has any effect if the item passed to the Scanner
			//constructor implements Closeable (which it does in this case).
			scanner.close();
		}
		if (hasFailed) {
			System.out.println("FAILED !!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		return pos;
	}

	private static void readPoint(String line, double[] point, int pos) {
		int k1 = line.indexOf(',');
		int k2 = line.indexOf(',', k1+1);
		double f1 = Double.parseDouble(line.substring(0, k1));
		double f2 = Double.parseDouble(line.substring(k1+1, k2));
		double f3 = Double.parseDouble(line.substring(k2+1));
		if (f3 != 0.0) {
			throw new IllegalStateException("f3=" + f3);
		}
		if (f1>0 || f2 < 0) {
			throw new IllegalArgumentException("f1/f2=" + f1 + " / " + f2);
		}

		point[pos+0] = f1;
		point[pos+1] = f2;
	}

	private static void log(Object aObject){
		System.out.println(String.valueOf(aObject));
	}

}
