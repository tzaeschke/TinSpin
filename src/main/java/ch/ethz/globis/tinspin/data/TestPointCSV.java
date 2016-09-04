/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import ch.ethz.globis.tinspin.TestStats;

/**
 * @author Tilmann Zaeschke
 */
public class TestPointCSV extends TestPoint {

	private static final char DEL = ' '; //delimiter
	private final TestStats S;
	
	protected TestPointCSV(Random R, TestStats S) {
		super(R, S);
		this.S = S;
	}

	/**
	 * Read open street map data from a database.
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		
		paramStr = "D:\\data\\ELKI\\";
		//paramStr += "mickey-mouse.csv";
		//paramStr += "scale-d-10d.csv";
		//paramStr += "scale-d-20d.csv";
		//paramStr += "aloi-8d.csv";
		paramStr += "aloi-27d.csv";
		
		log("Running: TestCSV(" + paramStr + ")");
		
		return readFile(paramStr, Integer.MAX_VALUE);
	}

	@Override
	public void queryCuboid(int resultsPerQuery, double[] xyz, double[] len) {
		// Adrien 06/02/2014
		// added for compatibility, only used for CUSTOM tests
	}
	
	private double[] readFile(String pathName, int maxPoints) {
		File f = new File(pathName);
		if (!f.exists()) {
			throw new IllegalArgumentException("File not found: " + pathName);
		}

		log("Reading file: " + f.getName());

		ArrayList<double[]> dataA = new ArrayList<>();
		readFile(f, dataA, maxPoints);

		int dim = dataA.get(0).length;
		double[] data = new double[dataA.size()*dim];
		for (int i = 0; i < dataA.size(); i++) {
			System.arraycopy(dataA.get(i), 0, data, i*dim, dim);
		}
		S.cfgNDims = dim;
		S.cfgNEntries = dataA.size();
		S.paramStr = paramStr;
		DIM = S.cfgNDims;
		return data;
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private final void readFile(File fFile, ArrayList<double[]> data, int MAX_E) {
		//Note that FileReader is used, not File, since File is not Closeable
		Scanner scanner;
		try {
			scanner = new Scanner(new BufferedReader(new FileReader(fFile)));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		int dim = -1;
		
		String nl = null;
		try {
			int nLines = 0;
			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.startsWith("##")) {
					continue;
				}
				nLines++;
				if (dim == -1) {
					String[] columns = line.split(Character.toString(DEL));
					dim = 0;
					while (dim < columns.length) {
						if (columns[dim].startsWith("\"")) {
							break;
						}
						dim++;
					}
				}

				double[] d = new double[dim];
				readPoint(line, d);
				data.add(d);

				if (data.size() >= MAX_E) {
					return;
				}
			}
			
			System.out.println("Lines: " + nLines);
			System.out.println("Points: " + data.size());
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
	}

	private static void readPoint(String line, double[] point) {
		int k1 = 0; 
		int k2 = line.indexOf(DEL);
		for (int i = 0; i < point.length; i++) {
			double f1 = Double.parseDouble(line.substring(k1, k2));
			point[i] = f1;
			k1 = k2;
			k2 = line.indexOf(DEL, k2+1);
		}
	}

}
