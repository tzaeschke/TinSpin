/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import ch.ethz.globis.tinspin.TestStats;

/**
 * Generator for Skyline query test data, based on the algorithm used by
 * S Chester, D Sidlauskas, I Assent, KS Bøgh: 
 * "Scalable Parallelization of Skyline Computation for Multi-core Processors", ICDE'15
 * 
 * C++ source code kindly provided by the authors above.
 * 
 * 
 * 
 * Borzsonyi, Kossmann and Stocker (2001) use a different generator:
 * 
 * indep: 
 * for this type of database, all attribute values are generated independently using a uniform 
 * distribution. Figure 10 shows such an independent database with 1000 points and d = 2. 
 * Points which are part of the Skyline are marked as bold circles. In Figure 10, twelve points 
 * are part of the Skyline.
 * 
 * corr: 
 * a correlated database represents an environment in which points which are good in one dimension
 * are also good in the other dimensions. For instance, students which have a good publication 
 * record typically also do well in their preliminaries. We generate a random point in the 
 * correlated database as follows. First we select a plane perpendicular to the line from 
 * (0, . . . , 0) to (1, . . . , 1) using a normal distribution; the new point will be in that 
 * plane. We use a normal distribution to select the plane so that more points are in the middle 
 * than at the ends. Within the plane, the individual attribute values are again generated using a 
 * normal distribution; this makes sure that most points are located close to the line from 
 * (0, . . . 0) to (1, . . . 1). Figure 11 shows a correlated database with 1000 points for d = 2.
 * The Skyline of this database contains four points.
 * 
 * anti: 
 * an anti-correlated database represents an environment in which points which are good in one 
 * dimension are bad in one or all of the other dimensions; hotels seem to fall into this category. 
 * As for a correlated database, we generate a point by first selecting a plane perpendicular to 
 * the line from (0, . . . , 0) to (1, . . . , 1) using a normal distribution. We use a normal 
 * distribution with very small variance so that all points are placed into planes which are close 
 * to the plane through the point (0.5, . . . , 0.5). Within the plane, the individual attribute 
 * values are generated using a uniform distribution. Figure 12 shows an anti-correlated database 
 * with 1000 points for d = 2. The Skyline of this database contains 36 points.
 * 
 * 
 * @author Tilmann Zaeschke
 */
public class TestPointSky extends TestPoint {

	public static final double P1_CORRELATED = 1.0;
	public static final double P1_INDEPENDENT = 2.0;
	public static final double P1_ANTICORRELATED = 3.0;
	
	public static boolean USE_TZ = false;
	
	public static boolean PRINT = false;
	
	/**
	 * Data set type selection via param1:
	 * <p>
	 * 1.0 -> correlated		<br>
	 * 2.0 -> independent  		<br>
	 * 3.0 -> anti-correlated	<br>
	 * 
	 * @param R
	 * @param S
	 */
	public TestPointSky(Random R, TestStats S) {
		super(R, S);
	}

	/**
	 * @return Elements
	 */
	@Override
	public double[] generate() {
		log("Running: TestSkyline(" + param1 + ")");
		double[] data = new double[getN()*DIM];
		
		ArrayWriter f = new DoubleAW(data);
		if (USE_TZ) {
			if (param1 == P1_CORRELATED) {
				generateDataCorrelatedTZ(f);
			} else if (param1 == P1_INDEPENDENT) {
				generateDataIndependentEqually(f, getN(), DIM);
			} else if (param1 == P1_ANTICORRELATED) {
				generateDataAnticorrelated(f, getN(), DIM);
			} else {
				throw new IllegalArgumentException("param1=" + param1);
			}
		} else {
			if (param1 == P1_CORRELATED) {
				generateDataCorrelated(f, getN(), DIM);
			} else if (param1 == P1_INDEPENDENT) {
				generateDataIndependentEqually(f, getN(), DIM);
			} else if (param1 == P1_ANTICORRELATED) {
				generateDataAnticorrelated(f, getN(), DIM);
			} else {
				throw new IllegalArgumentException("param1=" + param1);
			}
		}
		return data;
	}

	// ***************************************************************************
	// ** Datengenerierung **
	// ***************************************************************************

	private static double sqr(double x) {
		return x*x;
	}

	private int Statistics_Count;
	private double[] Statistics_SumX;
	private double[] Statistics_SumXsquared;
	private double[] Statistics_SumProduct;

	private void InitStatistics(int Dimensions)
	// ==============
	// initialisiert ZÃ¤hlvariablen der Statistik
	{
		Statistics_SumX = new double[Dimensions];
		Statistics_SumXsquared = new double[Dimensions];
		Statistics_SumProduct = new double[Dimensions * Dimensions];

		Statistics_Count = 0;
		for (int d = 0; d < Dimensions; d++) {
			Statistics_SumX[d] = 0.0;
			Statistics_SumXsquared[d] = 0.0;
			for (int dd = 0; dd < Dimensions; dd++)
				Statistics_SumProduct[d * Dimensions + dd] = 0.0;
		}
	}

	private void EnterStatistics(int Dimensions, double[] x)
	// ===============
	// registiriert den Vektor "x" fÃ¼r die Statistik
	{
		Statistics_Count++;
		for (int d = 0; d < Dimensions; d++) {
			Statistics_SumX[d] += x[d];
			Statistics_SumXsquared[d] += x[d] * x[d];
			for (int dd = 0; dd < Dimensions; dd++)
				Statistics_SumProduct[d * Dimensions + dd] += x[d] * x[dd];
		}
	}

	private void OutputStatistics(int Dimensions)
	// ================
	// gibt die Statistik aus
	{
		for (int d = 0; d < Dimensions; d++) {
			double E = Statistics_SumX[d] / Statistics_Count;
			double V = Statistics_SumXsquared[d] / Statistics_Count - E * E;
			double s = Math.sqrt(V);
			logf("E[X%d]=%5.2f Var[X%d]=%5.2f s[X%d]=%5.2f\n", d + 1, E, d + 1, V,
					d + 1, s);
		}
		logf("\nKorrelationsmatrix:\n");
		for (int d = 0; d < Dimensions; d++) {
			for (int dd = 0; dd < Dimensions; dd++) {
				double Kov = (Statistics_SumProduct[d * Dimensions + dd]
						/ Statistics_Count)
						- (Statistics_SumX[d] / Statistics_Count)
						* (Statistics_SumX[dd] / Statistics_Count);
				double Cor =
						Kov
						/ Math.sqrt(
								Statistics_SumXsquared[d]
										/ Statistics_Count- sqr(Statistics_SumX[d] / Statistics_Count))
										/ Math.sqrt(
												Statistics_SumXsquared[dd]
														/ Statistics_Count- sqr(Statistics_SumX[dd] / Statistics_Count));
				logf(" %5.2f", Cor);
			}
			logf("\n");
		}
		logf("\n");
	}

	private void logf(String format, Object ... args) {
		if (PRINT) {
			System.out.printf(format, args);
		}
	}
	
	private double RandomEqual(double min, double max)
	// ===========
	// liefert eine im Intervall [min,max[ gleichverteilte Zufallszahl
	{
		double x = R.nextDouble();//(double) rand() / MAXINT;
		return x * (max - min) + min;
	}

	private double RandomPeak(double min, double max, int dim)
	// ==========
	// liefert eine Zufallsvariable im Intervall [min,max[
	// als Summe von "dim" gleichverteilten Zufallszahlen
	{
		double sum = 0.0;
		for (int d = 0; d < dim; d++)
			sum += RandomEqual(0, 1);
		sum /= dim;
		return sum * (max - min) + min;
	}

	private double RandomNormal(double med, double var)
	// ============
	// liefert eine normalverteilte Zufallsvariable mit Erwartungswert med
	// im Intervall ]med-var,med+var[
	{
		//TODO wtf?  12???
		//TODO wtf variance=limit!!! -> Did they mean standard deviation? Still wrong!!!   
		return RandomPeak(med - var, med + var, 12);
	}

	private void generateDataIndependentEqually(ArrayWriter f, int Count, int Dimensions)
	// ===================
	// generiert in der Datei "f" "Count" gleichverteilte DatensÃ¤tze
	{
		InitStatistics(Dimensions);
		for (int i = 0; i < Count; i++) {
			double[] x = new double[Dimensions];
			for (int d = 0; d < Dimensions; d++) {
				x[d] = RandomEqual(0, 1);
				f.write(x[d]);
			}
			EnterStatistics(Dimensions, x);
			f.writeln();
		}
		OutputStatistics(Dimensions);
	}

	private void generateDataCorrelated(ArrayWriter f, int Count, int Dimensions)
	// ======================
	// generiert in der Datei "f" "Count" korrelierte DatensÃ¤tze
	{
		InitStatistics(Dimensions);
		double[] x = new double[Dimensions];
		int i = 0;
		again: 
			while (i < Count) {
				double v = RandomPeak(0, 1, Dimensions);
				for (int d = 0; d < Dimensions; d++) {
					x[d] = v;
				}
				double l = v <= 0.5 ? v : 1.0 - v;
				for (int d = 0; d < Dimensions; d++) {
					double h = RandomNormal(0, l);
					x[d] += h;
					x[(d + 1) % Dimensions] -= h;
				}
				for (int d = 0; d < Dimensions; d++) {
					if (x[d] < 0 || x[d] >= 1) {
						continue again;
					}
				}
				for (int d = 0; d < Dimensions; d++) {
					f.write(x[d]);
				}
				i++;
				EnterStatistics(Dimensions, x);
				f.writeln();
			}
		OutputStatistics(Dimensions);
	}

	/**
	 * Creates a random value with normal distribution with avg=0.0, std.dev=1.0 and v<1.
	 * @return Normal random value |v|< 1.0
	 */
	private double randomNormal(double stdDev) {
		double r;
		do {
			r = R.nextGaussian() * stdDev;
		} while (Math.abs(r) >= 1.0);
		return r;
	}
	
	private void generateDataCorrelatedTZ(ArrayWriter f)	{
		InitStatistics(DIM);
		double STD_DEV_DIST = 1.0; //compress distance on diagonal to (0,0) 
		double STD_DEV_WIDTH = 0.5; //compress distance from diagonal
		double dimScale = Math.sqrt(DIM); //Stretch to diagonal of unit-cube 
		double[] x = new double[DIM];
		for (int n = 0; n < getN(); n++) {
			boolean ok;
			do {
				//shift plane along diagonal by avg 0.5
				double dist = randomNormal(STD_DEV_DIST)/2.0 +0.5; //avg=0.0; dev=1.0 --> avg=0.5, dev=0.5
				dist *= dimScale; 
				for (int i = 0; i < DIM; i++) {
					x[i] = dist;
				}
				//create random cloud in plane perpendicular to diagonal
				for (int i = 0; i < DIM; i++) {
					double r = randomNormal(STD_DEV_WIDTH)/2.0; //avg=0.0; stdDev=1.0 --> stdDev=0.5
					r *= dimScale;
//					for (int j = 0; j < DIM; j++) {
//						x[j] *= (j == i) ? -r : r;
//					}				
					x[i] *= r;
				}
				//check
				ok = true;
				for (int i = 0; i < DIM; i++) {
					if (x[i] < 0.0 || x[i] >= 1.0) {
						ok = false;
						break;
					}
				}
			} while (!ok);
//		while (true) {
//			//create random cloud in plane perpendicular to x-axis
//			for (int i = 1; i < DIM; i++){
//				double r = randomNormal()*COMPRESS; 
//			}
//			//rotate plane
//
//			//shift plane along diagonal by avg 0.5
//			double dist = randomNormal()/2 +1; //avg=0,dev=1 --> avg=0.5, dev=0.5
//		}
		
			for (double d: x) {
				f.write(d);
			}
			EnterStatistics(DIM, x);
			f.writeln();
		}
		OutputStatistics(DIM);
	}

	private void generateDataAnticorrelated(ArrayWriter f, int Count, int Dimensions)
	// ==========================
	// generiert in der Datei "f" "Count" antikorrelierte DatensÃ¤tze
	{
		InitStatistics(Dimensions);
		double[] x = new double[Dimensions];
		int i = 0;
		again: 
			while (i < Count) {
				double v = RandomNormal(0.5, 0.25);
				for (int d = 0; d < Dimensions; d++) {
					x[d] = v;
				}
				double l = v <= 0.5 ? v : 1.0 - v;
				for (int d = 0; d < Dimensions; d++) {
					double h = RandomEqual(-l, l);
					x[d] += h;
					x[(d + 1) % Dimensions] -= h;
				}
				for (int d = 0; d < Dimensions; d++) {
					if (x[d] < 0 || x[d] >= 1) {
						continue again;
					}
				}
				for (int d = 0; d < Dimensions; d++) {
					f.write(x[d]);
				}
				i++;
				EnterStatistics(Dimensions, x);
				f.writeln();
			}
		OutputStatistics(Dimensions);
	}

	private void GenerateData(int Dimensions, char Distribution, int Count, String FileName)
	// ============
	// generierte eine Datei mit zufÃ¤lligen Daten
	{
		if (Count <= 0) {
			System.out.printf("UngÃ¼ltige Anzahl von Punkten.\n");
			return;
		}
		if (Dimensions < 2) {
			System.out.printf("UngÃ¼ltige Anzahl von Dimensionen.\n");
			return;
		}
		switch (Distribution) {
		case 'E':
		case 'e':
			Distribution = 'E';
			break;
		case 'C':
		case 'c':
			Distribution = 'C';
			break;
		case 'A':
		case 'a':
			Distribution = 'A';
			break;
		default:
			System.out.printf("UngÃ¼ltige Verteilung.\n");
			return;
		}

		//		File f = fopen(FileName, "wt");
		//		if (f == null) {
		//			System.out.printf("Kann Datei \"%s\" nicht anlegen.\n", FileName);
		//			return;
		//		}
		try (Writer out = new BufferedWriter(new FileWriter(FileName));) {
			ArrayWriter f = new FileAW(out);
			//fprintf(f, "%d %d\n", Count, Dimensions);
			out.write(Count + " " + Dimensions + "\n");
			switch (Distribution) {
			case 'E':
				generateDataIndependentEqually(f, Count, Dimensions);
				break;
			case 'C':
				generateDataCorrelated(f, Count, Dimensions);
				break;
			case 'A':
				generateDataAnticorrelated(f, Count, Dimensions);
				break;
			}
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.printf("%d Punkte generiert, Datei \"%s\".\n", Count, FileName);
	}


	public static void main(String[] args)
	// ====
	// main program
	{
		if (args.length == 3) {
			int N = Integer.parseInt(args[3]);
			int DIM = Integer.parseInt(args[1]);
			char c = args[2].charAt(0);
			TestStats S = new TestStats(null, null, N, DIM, false, -1);
			switch (c) {
			case 'E':
				S = new TestStats(null, null, N, DIM, false, 2.0);
				break;
			case 'C':
				S = new TestStats(null, null, N, DIM, false, 1.0);
				break;
			case 'A':
				S = new TestStats(null, null, N, DIM, false, 3.0);
				break;
			}
			TestPointSky t = new TestPointSky(new Random(0), S);
			t.generate();
			return;
		}

		if (args.length != 4) {
			System.out.printf("generate - Generierung von Testdaten fÃ¼r Skyline\n\n");
			System.out.printf("Syntax: generate <Dimensionen> <Verteilung> <Anzahl> <Datei>\n");
			System.out.printf(
					" mit Verteilung = E(qually) | C(orrelated) | A(nti-correlated)\n\n");
		} else {
			int N = Integer.parseInt(args[2]);
			int DIM = Integer.parseInt(args[0]);
			TestStats S = new TestStats(null, null, N, DIM, false, -1);
			TestPointSky t = new TestPointSky(new Random(0), S);
			t.GenerateData(DIM, args[1].charAt(0), N, args[3]);
		}
	}


	private interface ArrayWriter {
		void write(double d);
		void writeln(double d);
		void writeln();
	}

	private static class FileAW implements ArrayWriter {

		private final Writer w;

		public FileAW(Writer w) {
			this.w = w;
		}

		@Override
		public void write(double d) {
			//fprintf(f, "%8.6f ", x[d]);
			String s1 = String.format("%8.6f", d);
			try {
				w.write(s1);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void writeln(double d) {
			write(d);
			writeln();
		}

		@Override
		public void writeln() {
			//fprintf(f, "\n");
			String s1 = String.format("%n");
			try {
				w.write(s1);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

	}

	private static class DoubleAW implements ArrayWriter {

		private final double[] w;
		private int pos = 0;

		public DoubleAW(double[] w) {
			this.w = w;
		}

		@Override
		public void write(double d) {
			w[pos++]=d;
		}

		@Override
		public void writeln(double d) {
			write(d);
			writeln();
		}

		@Override
		public void writeln() {
			//nothing
		}

	}

}
