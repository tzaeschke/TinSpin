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
import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import ch.ethz.globis.phtree.PhTreeSolidF;
import ch.ethz.globis.phtree.nv.PhTreeNVSolidF.PHREntry;
import ch.ethz.globis.tinspin.TestInstances;
import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.util.TestPerf;
import ch.ethz.globis.tinspin.wrappers.Candidate;
import ch.ethz.globis.tinspin.wrappers.RectanglePHC;

/**
 * Data generation according to the data used in:
 * "TOUCH: In-Memory Spatial Join by Hierarchical Data-Oriented Partitioning"
 * by
 * S. Nobari, F. Tauheed, T. Heinis, P. Karras, S. Bressan, A. Ailamaki
 * 
 * <p>
 * 
 * param1: 0=even, 1=gauss, greater than 1 = gauss spots
 * <br>
 * param2: EPSILON, usually 5 or 10
 * 
 * @author Tilmann Zaeschke
 */
public class TestRectangleTOUCH extends TestRectangle {

	private static boolean USE_ITERATOR_QUERY = true; 
	private static boolean CONSOLIDATE_RESULTS = false; 
//	private static boolean USE_ITERATOR_QUERY = false; 
//	private static boolean CONSOLIDATE_RESULTS = true; 
	
	private static boolean READ_FROM_FILE = false;
	private static int READ_FROM_FILE_CYCLE = 0;
	private double[] allQueries;
	private int aqPos = 0;
	
	private static final double LEN = 1000;//6.6e-6;//1000;
	private static final double BOX_LEN = LEN/1000.;//1;
	private static final int MAX_N_SPOTS = 100;
	
	private static final double[] EPS = {LEN/200, LEN/100, 0};//{5, 10};
	
	private double[][] spotsData;
	private double[][] spotsQueries;
	
	
	public TestRectangleTOUCH(Random R, TestStats S) {
		super(R, S);
	}
	
	
	/**
	 */
	@Override
	public double[] generate() {
		log("Running: TestRectangleTOUCH");
		spotsData = generateSpots();
		spotsQueries = generateSpots();
		return generate(getN(), param1, spotsData);
	}
		
	public double[] generate(int N, double param1, double[][] spots) {
		if (READ_FROM_FILE) {
			double[] ret;
			if (READ_FROM_FILE_CYCLE == 0) {
				ret = readFromFile("D:\\data\\EPFL\\data-uniform-2000000.txt");
				//N = ret.length/(2*DIM);
			} else if (READ_FROM_FILE_CYCLE == 1) {
				allQueries = readFromFile("D:\\data\\EPFL\\data-clustered-1000000.txt");
				ret = new double[2*DIM];
				System.arraycopy(allQueries, 0, ret, 0, 2*DIM);
				aqPos += DIM*2;
			} else {
				ret = new double[2*DIM];
				System.arraycopy(allQueries, aqPos, ret, 0, 2*DIM);
				aqPos += DIM*2;
			}
			READ_FROM_FILE_CYCLE++;
			return ret;
		}
		if (param1 <= 1) {
			return generateNoSpots(N);
		}
		return generateWithSpots(N, spots);
	}
	
	private double[] generateNoSpots(int N) {
		double[] data = new double[N*DIM*2];

		//loop over clusters
		for (int i = 0; i < N; i++) {
			int ii = i*DIM*2;
			for (int d = 0; d < DIM; d++) {
				data[ii + d] = createPos();
				data[ii+DIM+d] = data[ii + d] + BOX_LEN * R.nextDouble();
			}
		}
		return data;
	}
	
	private double createPos() {
		if (param1 == 0.0) {
			return R.nextDouble()*(LEN-BOX_LEN);
		}
		double r;
		do {
			r = LEN/2 + R.nextGaussian()*LEN*0.25; //mu = 500, sigma=250
		} while (r < 0 || r > LEN);
		return r;
	}

	private double[][] generateSpots() {
		int nSpots = R.nextInt(MAX_N_SPOTS-1)+1; //avoid 0
		double[][] spots = new double[nSpots][DIM];
		for (int i = 0; i < spots.length; i++) {
			//spots[i] = new double[DIM];
			for (int d = 0; d < DIM; d++) {
				spots[i][d] = R.nextDouble()*LEN;
			}
		}
		return spots;
	}
		
	private double[] generateWithSpots(int N, double[][] spots) {
		double[] data = new double[N*DIM*2];
		
		//loop over clusters
		for (int i = 0; i < N; i++) {
			int ii = i*DIM*2;
			int spotID = R.nextInt(spots.length);
			for (int d = 0; d < DIM; d++) {
				double r = 0;
				double[] spot = spots[spotID];
				do {
					r = spot[d] + R.nextGaussian()*LEN*0.022; //sigma=220
				} while (r < 0 || r > LEN);

				data[ii + d] = r;
				data[ii+DIM+d] = data[ii + d] + BOX_LEN * R.nextDouble();
			}
		}
		return data;
	}
	
	@Override
	public void generateQuery(double[] min, double[] max, 
			final double maxLen, final double avgQVol) {
		double[] q = generate(1, param1, spotsQueries);
		for (int i = 0; i < DIM; i++) {
			min[i] = q[i]-param2;
			max[i] = q[i+DIM]+param2;
		}
		//System.out.println("q: " + Arrays.toString(min) + " / " + Arrays.toString(max));
	}

	private static void runWithArgs(String[] args) {
		if (args.length < 5) {
			System.out.println("ERROR: At least 5 arguemnts required, found: " + args.length);
			System.out.println("Example: TestRunner 1 PHC 1000 10000 3 5");
			System.out.println("Example: TestRunner 1 PHC 1000 10000 3 10 0 3 myData.csv");
			System.out.println("Args: TestRectangleTOUCH [TEST] [INDEX] [SIZE_A] [SIZE_B] [DIM] "
					+ "[EPS] [SEED default=0] [REPEAT default=3] [optional: <exportFilename>]");
			System.out.println("Args: ");
			System.out.println("   [TEST]  : 0=even, 1=gauss, 2=gauss with spots");
			System.out.println("   [INDEX] : PHC, XTR");
			System.out.println("   [SIZE_A], [SIZE_B]: size of the two datasets");
			System.out.println("   [DIM]   : dimensionality, usually 3");
			System.out.println("   [EPS]   : epsilon, usually 5 or 10");
			System.out.println("   [SEED]  : random generator seed, default=0");
			System.out.println("   [REPEAT]: repeated runs, default=3");
			System.out.println("The SEED is used for the random generator, REPEAT determines how"
					+ " many iterations are run.");
			System.out.println("SEED and REPEAT have default values and don’t need to be provided "
					+ "(unless you want to export). For export, REPEAT is internally set to ‘1’. ");
			System.out.println("Note that you can’t skip one parameter and still use the next one, "
					+ "so even for EXPORT, you still have to provide REPEAT, even if it is "
					+ "ignored.");
			return;
		}

		int param0 = Integer.parseInt(args[0]);
		
		TestInstances.IDX idx;
		try {
			idx = TestInstances.IDX.valueOf(args[1]);
		} catch (IllegalArgumentException e) {
			System.out.println("Index not recognised: " + args[1]);
			System.out.print("Please choose one of: ");
			for (TestInstances.IDX t: TestInstances.IDX.values()) {
				System.out.print(t.name() + ", ");
			}
			return;
		}
		
		int n_a = Integer.parseInt(args[2]);
		int n_b = Integer.parseInt(args[3]);
		int dim = Integer.parseInt(args[4]);
		double eps = Double.parseDouble(args[5]);
		int seed = (args.length > 6 ? Integer.parseInt(args[6]) : 0);
		int repeat = (args.length > 7 ? Integer.parseInt(args[7]) : 3);
		
		TestStats S = new TestStats(TestInstances.TST.TOUCH, idx, n_b, dim, true, param0, eps);

		if (args.length>8) {
			Path path = FileSystems.getDefault().getPath(args[8]);
			TestRectangleTOUCH t = new TestRectangleTOUCH(new Random(seed), S);
			t.run(n_a, path, S);
		}

		for (int i = 0; i < repeat; i++) {
			TestRectangleTOUCH t = new TestRectangleTOUCH(new Random(seed), S);
			t.run(n_a, null, S);
			//System.out.println(S);
		}
		//System.out.println(S);
		return;
	}

	public static void main(String[] args) {
		if (args.length > 0) {
			runWithArgs(args);
			return;
		}
		
		
		//0: tiny -> 1417
		//int N_A = 1*1000;
		//int N_B = 64*1000;
		
		//A: small -> 120173
		//int N_A = 10*1000;
		//int N_B = 640*1000;
		
		//B: big
		//int N_A = 160*1000;
		//int N_B = 1600*1000;
		
		//C: Neuroscience   --> 16019803 (PHT)  / 2252508 (e-6)
		//                  --> 2062737 (1/5/1000) / 14342128 (1/10/1000)
		int N_A = 644*1000;
		int N_B = 1285*1000;

		//int N_A = 1288000;
		//int N_B = 2570000;
		
		//D: from EPFL:    --> MX-CIF: 2045 / PHC2:2045 / PHC:2045(2040) / PHC_IPP:2045/2040
		//min/max: 0.00763619/999.976
		//min/max: -0.499834/999.5
		//int N_A = 1000*1000;
		//int N_B = 2*1000*1000;

		// param1: 0=even, 1=gauss, >1= gauss spots
		// param2: EPSILON, usually 5 or 10
		TestStats S = new TestStats(TestInstances.TST.TOUCH, TestInstances.IDX.CUSTOM, N_B, 3, true, 1, EPS[2]);
		for (int i = 0; i < 3; i++) {
			S.setSeed(0);;
			System.out.println("SEED: " + S.SEEDmsg);
			TestRectangleTOUCH t = new TestRectangleTOUCH(new Random(S.seed), S);
			t.run(N_A, null, S);
			//System.out.println(S);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void run(int N_A, Path exportFile, TestStats ts) {
		
		//load
		System.out.println("Create data...");
		double[] data = generate();
		System.out.println("mem=" + (Runtime.getRuntime().totalMemory()/1000000) + "MB");

		System.out.println("Load data...");
		Candidate tree = new RectanglePHC(ts);
		//Candidate tree = new RectanglePHC2(ts);
		//Candidate tree = new RectanglePHC_IPP(ts);
		//Candidate tree = new RectanglePHCRect(ts);
		//Node.AHC_LHC_BIAS=2.0;
		//Candidate tree = new RectangleArray(ts);
		//Candidate tree = RectangleXtree.create(ts);
		//Candidate tree = new RectangleMXCIF3D(ts);
		tree.load(data, DIM);
		System.out.println("mem=" + (Runtime.getRuntime().totalMemory()/1000000) + "MB");
		
		//join (queries)
		System.out.println("Create queries...");
		long n = 0;
		double[][] qA = new double[2*N_A][];
		for (int i = 0; i < N_A; i++) {
			double[] min = new double[DIM];
			double[] max = new double[DIM];
			generateQuery(min, max, -1, -1);
			qA[2*i] = min;
			qA[2*i+1] = max;
		}
		System.out.println("mem=" + (Runtime.getRuntime().totalMemory()/1000000) + "MB");
		
//		for (int i = 0; i < 10; i++) {
//			double[] min = qA[2*i];
//			double[] max = qA[2*i+1];
//			System.out.println(Arrays.toString(qA[2*i]) + "/" + Arrays.toString(qA[2*i+1]));
//			double[] dist = new double[DIM];
//			Arrays.setAll(dist, d -> max[d] - min[d]);
//			System.out.println(Arrays.toString(dist));
//		}
		
		if (exportFile != null) {
			export(exportFile, data, qA);
			return;
		}

		//load results
//		System.out.println("Loading results...");
//		double[] resultA = readFromFileResults("D:\\data\\EPFL\\results.txt");
//		int nR = resultA.length/6;
//		PhTreeRangeD results = new PhTreeRangeD(3);
//		for (int i = 0; i < nR; i++) {
//			double[] min = new double[DIM];
//			double[] max = new double[DIM];
//			for (int d = 0; d < 3; d++) {
//				min[d] = resultA[i*6+d];
//				max[d] = resultA[i*6+3+d];
//			}
//			results.insert(min, max);
//		}
//		System.out.println("Results: " + nR + "/" + results.size());
		
		
		
		System.out.println("Querying...");
		long t0 = System.currentTimeMillis();
		PhTreeSolidF<?> resultSet = PhTreeSolidF.create(DIM);
		int dupl = 0;
		int nCorrect = 0;
		int nWrong = 0;
		for (int i = 0; i < N_A; i++) {
			double[] min = qA[2*i];
			double[] max = qA[2*i+1];
			if (USE_ITERATOR_QUERY) {
				//use iterator query
				n += tree.query(min, max);
			} else {
				//use list query
				List<PHREntry> result = (List<PHREntry>) tree.queryToList(min, max);
				if (CONSOLIDATE_RESULTS) {
//					for (PHREntry e: result) {
//						if (results.contains(e)) {
//							nCorrect++;
//						} else {
//							nWrong++;
//							System.out.println("W:" + Arrays.toString(min) + "/" + Arrays.toString(max) +
//									" X " + Arrays.toString(e.lower()) + "/" + Arrays.toString(e.upper()));
//						}
//						if (min[0] < e.upper()[0] && max[0] > e.lower()[0] &&
//								min[1] < e.upper()[1] && max[1] > e.lower()[1] &&
//								min[2] < e.upper()[2] && max[2] > e.lower()[2]) {
//							
//						} else {
//							n--;
//						}
//					}
//					PhTreeRangeD resultSetLocal = new PhTreeRangeD(DIM);
//					for (PHREntry o: result) {
//						if (resultSet.insert(o)) {
//							dupl++;
//						}
//						if (resultSetLocal.insert(o)) {
//							throw new RuntimeException();
//						}
//					}
				}
				n += result.size();
			}
			if (i < 1000 && i%100 == 0) System.out.print(",");
			if (i < 10000 && i%1000 == 0) System.out.print(".");
		}
		long t1 = System.currentTimeMillis();
		System.out.println("Right/wrong: " + nCorrect + "/" + nWrong);
		System.out.println("mem=" + (Runtime.getRuntime().totalMemory()/1000000) + "MB");
		System.out.println("Total query time: " + (t1-t0));
		System.out.println("Query result size=" + n);
		System.out.println("Query result size consolidated =" + resultSet.size() + " dup=" + dupl);
		System.out.println("selectivity N_A=" + n/(double)(N_A));
		System.out.println("selectivity N_B=" + n/(double)(getN()));
		System.out.println("selectivity NP/NQ=" + n/((double)getN()*N_A));
		
//		TestDraw.draw(data, 3, MODE.LINES);
//		TestDraw.draw(qA, 3, MODE.RECTANGLES);
		System.out.println("Prof: x,a,b,ab " + TestPerf.STAT_X5 + " / " + 
				TestPerf.STAT_X5a + " / " + 
				TestPerf.STAT_X5b + " / " +
				TestPerf.STAT_X5ab);
	}


	private void export(Path exportFile, double[] data, double[][] qA) {
		String SEP = ", ";
		String NL = System.lineSeparator();
		Writer out;
		try {
			out = Files.newBufferedWriter(exportFile);
			try {
				//set A
				out.write("" + qA.length + SEP + DIM + NL);
				for (int i = 0; i < qA.length; i++) {
					for (int j = 0; j < DIM-1; j++) {
						out.write( Double.toString(qA[i][j]) + SEP);
					} 
					out.write( Double.toString(qA[i][DIM-1]) + NL);
				}
				
				//set B
				out.write("" + data.length/DIM + SEP + DIM + NL);
				for (int i = 0; i < data.length; i++) {
					if ((i+1) % DIM > 0) {
						out.write( Double.toString(data[i]) + SEP);
					} else {
						out.write( Double.toString(data[i]) + NL);
					}
				}
			} finally {
				out.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private double[] readFromFile(String pathName) {
		File f = new File(pathName);
		double[] data = new double[12*1000*1000];
		int pos = 0;
		System.out.println("Reading file: " + f.getName());


		pos = readFile(f, data, pos, DIM);

		data = Arrays.copyOf(data, pos);
		System.out.println("RF: doubles: " + pos);
		System.out.println("RF: Points: " + pos/(2*DIM));
		return data;
	}
	
	
	@SuppressWarnings("unused")
	private double[] readFromFileResults(String pathName) {
		File f = new File(pathName);
		double[] data = new double[100*1000];
		int pos = 0;
		System.out.println("Reading file: " + f.getName());


		pos = readFileResults(f, data, pos, DIM);

		data = Arrays.copyOf(data, pos);
		System.out.println("RF: doubles: " + pos);
		System.out.println("RF: Points: " + pos/(2*DIM));
		return data;
	}
	
	
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private static final int readFile(File fFile, double[] data, int pos, int DIM) {
		double min = Double.MAX_VALUE; 
		double max = -Double.MAX_VALUE; 
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
			while (scanner.hasNext()) {
				for (int i = 0; i < 2*DIM; i++) {
					//line = scanner.next();
					String token = scanner.next();
//					System.out.println("tk1: " + token);
					if (token.charAt(token.length()-1) == ',') {
						token = token.substring(0, token.length()-1);
					}
					double d = Double.parseDouble(token);
					max = d > max ? d : max;
					min = d < min ? d : min;
//					if (pos >= MAX_E) {
//						return pos;
//					}
					data[pos] = d;
					pos++;
				}
//				System.out.println("posX="+ pos + "  " +
//						data[pos-6]  + "/" + data[pos-5]   + "," +  
//						data[pos-4]  + "/" + data[pos-3]   + "," +  
//						data[pos-2]  + "/" + data[pos-1]);
//				System.out.println("pos="+ pos + "  " +
//						data[pos-6]  + "/" + data[pos-3]   + "," +  
//						data[pos-5]  + "/" + data[pos-2]   + "," +  
//						data[pos-4]  + "/" + data[pos-1]);
				//check
				if (data[pos-6] > data[pos-3] || 
						data[pos-5] > data[pos-2] || 
						data[pos-4] > data[pos-1]) {
					throw new RuntimeException("pos="+ pos + "  " +
							data[pos-6]  + "/" + data[pos-3]   + "," +  
							data[pos-5]  + "/" + data[pos-2]   + "," +  
							data[pos-4]  + "/" + data[pos-1]);
				}
			}
			
			System.out.println("Points: " + pos/(2*DIM));
			System.out.println("min/max: " + min + "/" + max);
			
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
	/** 
	 * Template method that calls {@link #processLine(String)}.  
	 * @param b2 
	 * @param entries2 
	 */
	private static final int readFileResults(File fFile, double[] data, int pos, int DIM) {
		double min = Double.MAX_VALUE; 
		double max = -Double.MAX_VALUE; 
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
			while (scanner.hasNext()) {
				for (int i = 0; i < 2*DIM; i++) {
					//line = scanner.next();
					String token = scanner.next();
//					System.out.println("tk1: " + token);
					if (token.charAt(token.length()-1) == ',') {
						token = token.substring(0, token.length()-1);
					}
					double d = Double.parseDouble(token);
					max = d > max ? d : max;
					min = d < min ? d : min;
					
					//DO NOT ADD
//					data[pos] = d;
//					pos++;
					
				}
				String token = scanner.next(); //X
				for (int i = 0; i < 2*DIM; i++) {
					//line = scanner.next();
					token = scanner.next();
//					System.out.println("tk1: " + token);
					if (token.charAt(token.length()-1) == ',') {
						token = token.substring(0, token.length()-1);
					}
					double d = Double.parseDouble(token);
					max = d > max ? d : max;
					min = d < min ? d : min;
					data[pos] = d;
					pos++;
					
				}
				//check
				if (data[pos-6] > data[pos-3] || 
						data[pos-5] > data[pos-2] || 
						data[pos-4] > data[pos-1]) {
					throw new RuntimeException("pos="+ pos + "  " +
							data[pos-6]  + "/" + data[pos-3]   + "," +  
							data[pos-5]  + "/" + data[pos-2]   + "," +  
							data[pos-4]  + "/" + data[pos-1]);
				}
			}
			
			System.out.println("Points: " + pos/(2*DIM));
			System.out.println("min/max: " + min + "/" + max);
			
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
}
