/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import ch.ethz.globis.phtree.PhTreeSolidF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhEntrySF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhIteratorSF;
import ch.ethz.globis.phtree.PhTreeSolidF.PhQuerySF;
import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.TestStats.IDX;
import ch.ethz.globis.tinspin.TestStats.TST;
import ch.ethz.globis.tinspin.data.AbstractTest;
import ch.ethz.globis.tinspin.data.TestPoint;
import ch.ethz.globis.tinspin.data.TestRectangle;
import ch.ethz.globis.tinspin.wrappers.Candidate;
import ch.ethz.globis.tinspin.wrappers.RectanglePHCF;


/**
 * This is a special test class to estimate pairwise overlap counts.
 *
 * @author Tilmann Zaeschke
 */
public class TestRunnerOV {
	
	private static final SimpleDateFormat FT = new SimpleDateFormat ("yyyy-MM-dd' 'HH:mm:ss");

	private final TestStats S;
	private Random R;
	private double[] data;
	private Candidate tree;
	private AbstractTest test = null;

	private static class Result {
		int k;
		double l;
		int N;
		int nOverlap;
		public Result(int k, double l, int N) {
			this.k = k; 
			this.l = l;
			this.N = N;
		}
		@Override
		public String toString() {
			return k + "\t" + l + "\t" + N + "\t" + nOverlap;
		}
	}
	
	public static void main(String[] args) {
		//-Xmx28G -XX:+UseConcMarkSweepGC -Xprof -XX:MaxInlineSize=0 -XX:FreqInlineSize=0 -XX:+UnlockDiagnosticVMOptions -XX:+PrintInlining 
		//-XX:+PrintHeapAtGC - Prints detailed GC info including heap occupancy before and after GC
		//-XX:+PrintTenuringDistribution - Prints object aging or tenuring information
	
		ArrayList<Result> list = new ArrayList<>();
		int N = 10;
//		list.add(new Result(1, 0.1, N));
//		list.add(new Result(1, 0.2, N));
//		list.add(new Result(1, 0.3, N));
//		list.add(new Result(1, 0.5, N));
//		list.add(new Result(2, 0.1, N));
//		list.add(new Result(2, 0.2, N));
//		list.add(new Result(2, 0.3, N));
//		list.add(new Result(2, 0.5, N));
//		list.add(new Result(3, 0.2, N));
//		list.add(new Result(3, 0.5, N));
//		list.add(new Result(4, 0.6, N));
//		list.add(new Result(4, 0.8, N));
//		list.add(new Result(5, 0.6, N));
//		list.add(new Result(5, 0.8, N));
//		list.add(new Result(6, 0.6, N));
//		list.add(new Result(6, 0.8, N));
//		list.add(new Result(8, 0.6, N));
//		list.add(new Result(8, 0.8, N));
//		list.add(new Result(10, 0.6, N));
//		list.add(new Result(10, 0.8, N));
//
//		N = 100;
//		list.add(new Result(1, 0.001, N));
//		list.add(new Result(1, 0.01, N));
//		list.add(new Result(1, 0.1, N));
//		list.add(new Result(1, 0.2, N));
//		list.add(new Result(2, 0.01, N));
//		list.add(new Result(2, 0.1, N));
//		list.add(new Result(2, 0.2, N));
//		list.add(new Result(2, 0.5, N));
//		list.add(new Result(3, 0.1, N));
//		list.add(new Result(3, 0.2, N));
//		list.add(new Result(3, 0.5, N));
//		list.add(new Result(4, 0.1, N));
//		list.add(new Result(4, 0.2, N));
//		list.add(new Result(4, 0.5, N));
//		list.add(new Result(5, 0.1, N));
//		list.add(new Result(5, 0.2, N));
//		list.add(new Result(5, 0.5, N));
//		list.add(new Result(6, 0.2, N));
//		list.add(new Result(6, 0.5, N));
//		list.add(new Result(6, 0.8, N));
//		list.add(new Result(8, 0.2, N));
//		list.add(new Result(8, 0.5, N));
//		list.add(new Result(8, 0.8, N));
//		list.add(new Result(10, 0.2, N));
//		list.add(new Result(10, 0.5, N));
//		list.add(new Result(10, 0.8, N));
//
//		N = 1000;
//		list.add(new Result(1, 0.001, N));
//		list.add(new Result(1, 0.01, N));
//		list.add(new Result(1, 0.1, N));
//		list.add(new Result(2, 0.001, N));
//		list.add(new Result(2, 0.01, N));
//		list.add(new Result(2, 0.1, N));
//		list.add(new Result(3, 0.1, N));
//		list.add(new Result(3, 0.2, N));
//		list.add(new Result(3, 0.5, N));
//		list.add(new Result(4, 0.1, N));
//		list.add(new Result(4, 0.2, N));
//		list.add(new Result(4, 0.5, N));
//		list.add(new Result(5, 0.1, N));
//		list.add(new Result(5, 0.2, N));
//		list.add(new Result(5, 0.5, N));
//		list.add(new Result(6, 0.1, N));
//		list.add(new Result(6, 0.2, N));
//		list.add(new Result(6, 0.5, N));
//		list.add(new Result(8, 0.1, N));
//		list.add(new Result(8, 0.2, N));
//		list.add(new Result(8, 0.5, N));
//		list.add(new Result(10, 0.1, N));
//		list.add(new Result(10, 0.2, N));
//		list.add(new Result(10, 0.5, N));
//	
//		N = 10000;
//		list.add(new Result(1, 0.0001, N));
//		list.add(new Result(1, 0.001, N));
//		list.add(new Result(1, 0.01, N));
//		list.add(new Result(1, 0.1, N));
//		list.add(new Result(2, 0.001, N));
//		list.add(new Result(2, 0.01, N));
//		list.add(new Result(2, 0.1, N));
//		list.add(new Result(3, 0.05, N));
//		list.add(new Result(3, 0.1, N));
//		list.add(new Result(3, 0.2, N));
//		list.add(new Result(3, 0.5, N));
//		list.add(new Result(4, 0.1, N));
//		list.add(new Result(4, 0.2, N));
//		list.add(new Result(4, 0.5, N));
//		list.add(new Result(5, 0.1, N));
//		list.add(new Result(5, 0.2, N));
//		list.add(new Result(5, 0.5, N));
//		list.add(new Result(6, 0.1, N));
//		list.add(new Result(6, 0.2, N));
//		list.add(new Result(6, 0.5, N));
//		list.add(new Result(8, 0.1, N));
//		list.add(new Result(8, 0.2, N));
//		list.add(new Result(8, 0.5, N));
//		list.add(new Result(10, 0.1, N));
//		list.add(new Result(10, 0.2, N));
//		list.add(new Result(10, 0.5, N));
//		
//		N = 100000;
//		list.add(new Result(1, 0.00001, N));
//		list.add(new Result(1, 0.0001, N));
//		list.add(new Result(1, 0.001, N));
//		list.add(new Result(1, 0.01, N));
//		list.add(new Result(2, 0.0001, N));
//		list.add(new Result(2, 0.001, N));
//		list.add(new Result(2, 0.01, N));
//		list.add(new Result(3, 0.001, N));
//		list.add(new Result(3, 0.01, N));
//		list.add(new Result(3, 0.1, N));
//		list.add(new Result(4, 0.02, N));
//		list.add(new Result(4, 0.05, N));
//		list.add(new Result(4, 0.1, N));
//		list.add(new Result(5, 0.02, N));
//		list.add(new Result(5, 0.05, N));
//		list.add(new Result(5, 0.1, N));
//		list.add(new Result(5, 0.2, N));
//		list.add(new Result(6, 0.02, N));
//		list.add(new Result(6, 0.05, N));
//		list.add(new Result(6, 0.1, N));
//		list.add(new Result(6, 0.2, N));
//		list.add(new Result(6, 0.4, N));
//		list.add(new Result(8, 0.02, N));
//		list.add(new Result(8, 0.05, N));
//		list.add(new Result(8, 0.1, N));
//		list.add(new Result(8, 0.2, N));
//		list.add(new Result(8, 0.4, N));
//		list.add(new Result(10, 0.02, N));
//		list.add(new Result(10, 0.05, N));
//		list.add(new Result(10, 0.1, N));
//		list.add(new Result(10, 0.2, N));
//		list.add(new Result(10, 0.4, N));
//
		N = 1000*1000;
//		list.add(new Result(1, 1.0e-6, N));
//		list.add(new Result(1, 1.0e-5, N));
//		list.add(new Result(1, 1.0e-4, N));
//		list.add(new Result(1, 2.0e-4, N));
//		list.add(new Result(1, 5.0e-4, N));
//		
//		list.add(new Result(2, 1.0e-4, N));
//		list.add(new Result(2, 0.001, N));
//		list.add(new Result(2, 0.005, N));
//		list.add(new Result(2, 0.01, N));
//		list.add(new Result(2, 0.02, N));
//		
//		list.add(new Result(3, 0.001, N));
//		list.add(new Result(3, 0.002, N));
//		list.add(new Result(3, 0.005, N));
//		list.add(new Result(3, 0.01, N));
//		list.add(new Result(3, 0.02, N));
//		list.add(new Result(3, 0.03, N));
//		list.add(new Result(3, 0.04, N));
//		list.add(new Result(3, 0.05, N));
//
//		list.add(new Result(4, 0.01, N));
//		list.add(new Result(4, 0.02, N));
//		list.add(new Result(4, 0.03, N));
//		list.add(new Result(4, 0.05, N));
//		list.add(new Result(4, 0.08, N));
//		list.add(new Result(4, 0.1, N));
//		
//		list.add(new Result(5, 0.01, N));
//		list.add(new Result(5, 0.05, N));
//		list.add(new Result(5, 0.06, N));
//		list.add(new Result(5, 0.08, N));
//		list.add(new Result(5, 0.1, N));
//
//		list.add(new Result(6, 0.03, N));
//		list.add(new Result(6, 0.04, N));
//		list.add(new Result(6, 0.05, N));
//		list.add(new Result(6, 0.08, N));
//		list.add(new Result(6, 0.1, N));
//		list.add(new Result(6, 0.15, N));

		list.add(new Result(8, 0.1, N));
		list.add(new Result(8, 0.2, N));

		list.add(new Result(10, 0.1, N));

//		N = 10*1000*1000;
//		list.add(new Result(1, 1.0e-8, N));
//		list.add(new Result(1, 1.0e-7, N));
//		list.add(new Result(1, 1.0e-6, N));
//		
//		list.add(new Result(2, 1.0e-6, N));
//		list.add(new Result(2, 1.0e-5, N));
//		list.add(new Result(2, 1.0e-4, N));
//		
//		list.add(new Result(3, 1.0e-5, N));
//		list.add(new Result(3, 1.0e-4, N));
//		list.add(new Result(3, 1.0e-3, N));
//
//		list.add(new Result(4, 1.0e-4, N));
//		list.add(new Result(4, 1.0e-3, N));
//		list.add(new Result(4, 1.0e-2, N));

		
		for (int i = 0; i < list.size(); i++) {
			Result r = list.get(i);
			testMe(r);
			System.out.println(r);
			if (i % 10 == 0 || r.N >= 1000*1000) {
				printResult(list, i+1);
			}
		}

		printResult(list, list.size());
	}
	
	private static void printResult(ArrayList<Result> list, int pos) {
		System.out.println("All Results: " + pos + "/" + list.size());
		System.out.println("============");
		for (int i = 0; i < pos; i++) {
			Result r = list.get(i);
			System.out.println(r);
		}
	}
	
	private static void testMe(Result r) {
		
		final int DIM = r.k;
		final int N = r.N;
						
		//TestStats s0 = new TestStats(TST.CUBE, IDX.RSS, N, DIM, true, 1.0);
		TestStats s0 = new TestStats(TST.CUBE, IDX.PHCF, N, DIM, true, 1.);
		//TestStats s0 = new TestStats(TST.CLUSTER, IDX.PHC, N, DIM, false, 3.4);
		//TestStats s0 = new TestStats(TST.CUBE, IDX.PHCF, N, DIM, false, 1.0);
		//TestStats s0 = new TestStats(TST.OSM, IDX.PHC, N, 2, false, 1.0);
		s0.cfgWindowQueryRepeat = 100;
		s0.cfgPointQueryRepeat = 1000;
		s0.cfgUpdateSize = 1000;
		
		s0.cfgRectLen = r.l;
		
//		k=1 rectLen=1.0E-6
//				Checked elements: 1000000  vol=0.5002005580949878
//				Overlapping elements: 832557  vol=0.20808012649862298
//				Ratio: 0.20799669565653373  nSingle=416278
//				Ratio: 0.10404006324931149  nSingle=416278
		
//		k=1 rectLen=1.0E-5
//				Checked elements: 1000000  vol=5.0020055809495485
//				Overlapping elements: 8337346  vol=20.868168580527477
//				Ratio: 2.085980137647707  nSingle=4168673
//x				Ratio: 10.434084290263739  nSingle=4168673
		
//		k=1 rectLen=1.0E-4
//				Checked elements: 1000000  vol=50.02005580949742
//				Overlapping elements: 83372964  vol=2085.669629653612
//				Ratio: 20.84833369235867  nSingle=41686482
//x				Ratio: 1042.834814826806  nSingle=41686482
		
//		k=1 rectLen=2.0E-4
//				Checked elements: 1000000  vol=100.04011161899388
//				Overlapping elements: 166770650  vol=8343.517749490076
//				Ratio: 41.70086185662529  nSingle=83385325
//x				Ratio: 4171.758874745038  nSingle=83385325
				
//		k=1 rectLen=5.0E-4
//				Checked elements: 1000000  vol=250.10027904748483
//				Overlapping elements: 417012170  vol=52152.82545538025
//				Ratio: 104.26382900092315  nSingle=208506085
//				Ratio: 26076.412727690124  nSingle=208506085
				
//		k=2 rectLen=1.0E-4
//				Checked elements: 1000000  vol=0.0024996563419085187
//				Overlapping elements: 9634  vol=6.084811928830093E-6
//				Ratio: 0.0012171296963534321  nSingle=4817
//x				Ratio: 3.0424059644150465E-6  nSingle=4817
		
//		k=2 rectLen=0.001
//				Checked elements: 1000000  vol=0.24996563419084167
//				Overlapping elements: 970838  vol=0.060678532102246156
//				Ratio: 0.1213737486328217  nSingle=485419
//x				Ratio: 0.030339266051123078  nSingle=485419

//		k=2 rectLen=0.005
//				Checked elements: 1000000  vol=6.249140854771179
//				Overlapping elements: 24454957  vol=38.24792238508243
//				Ratio: 3.060254463289973  nSingle=12227478
//				Ratio: 19.123961192541216  nSingle=12227478

//		k=2 rectLen=0.01
//				Checked elements: 1000000  vol=24.996563419084815
//				Overlapping elements: 101303278  vol=616.2546254633894
//				Ratio: 12.326786989304308  nSingle=50651639
//				Ratio: 308.1273127316947  nSingle=50651639

//		k=2 rectLen=0.02
//				Checked elements: 1000000  vol=99.98625367633865
//				Overlapping elements: 399165027  vol=10006.791783041892
//				Ratio: 50.040837690721276  nSingle=199582513
//x				Ratio: 5003.395891520946  nSingle=199582513
				
//		k=3 rectLen=0.001
//				Checked elements: 1000000  vol=1.249541589012914E-4
//				Overlapping elements: 1048  vol=1.7000392921406453E-8
//				Ratio: 6.802651896859254E-5  nSingle=524
//x				Ratio: 8.500196460703226E-9  nSingle=524

//		k=3 rectLen=0.005
//				Checked elements: 1000000  vol=0.015619269862661767
//				Overlapping elements: 126254  vol=2.4686348950812934E-4
//				Ratio: 0.007902529749430296  nSingle=63127
//				Ratio: 1.2343174475406467E-4  nSingle=63127
		
//		k=3 rectLen=0.01
//				Checked elements: 1000000  vol=0.12495415890129391
//				Overlapping elements: 1021230  vol=0.015910135024388535
//				Ratio: 0.06366388747795326  nSingle=510615
//				Ratio: 0.007955067512194268  nSingle=510615
	
//		k=3 rectLen=0.02
//				Checked elements: 1000000  vol=0.9996332712103548
//				Overlapping elements: 8277075  vol=1.0390328732444825
//				Ratio: 0.5197070281516454  nSingle=4138537
//				Ratio: 0.5195164366222412  nSingle=4138537
				
//		k=3 rectLen=0.03
//				Checked elements: 1000000  vol=3.373762290334961
//				Overlapping elements: 28525900  vol=12.098961839019985
//				Ratio: 1.7930963710277807  nSingle=14262950
//				Ratio: 6.049480919509993  nSingle=14262950
		
//		k=3 rectLen=0.04
//				Checked elements: 1000000  vol=7.997066169682779
//				Overlapping elements: 68990952  vol=69.54010560386554
//				Ratio: 4.347851082406637  nSingle=34495476
//				Ratio: 34.77005280193277  nSingle=34495476
		
//		k=3 rectLen=0.05
//				Checked elements: 1000000  vol=15.619269862661277
//				Overlapping elements: 137476372  vol=271.330747622461
//				Ratio: 8.685769245561602  nSingle=68738186
//				Ratio: 135.6653738112305  nSingle=68738186
				
//		k=4 rectLen=0.01
//				Checked elements: 1000000  vol=6.250126417987038E-4
//				Overlapping elements: 10198  vol=4.0674313804350706E-7
//				Ratio: 3.253879288528901E-4  nSingle=5099
//				Ratio: 2.0337156902175353E-7  nSingle=5099

//		k=4 rectLen=0.02
//				Checked elements: 1000000  vol=0.010000202268779278
//				Overlapping elements: 168058  vol=1.0460576498183071E-4
//				Ratio: 0.005230182458829401  nSingle=84029
//				Ratio: 5.2302882490915354E-5  nSingle=84029
				
//		k=4 rectLen=0.03
//				Checked elements: 1000000  vol=0.05062602398569457
//				Overlapping elements: 875057  vol=0.002773436009875891
//				Ratio: 0.02739140654873058  nSingle=437528
//				Ratio: 0.0013867180049379456  nSingle=437528
			
//		k=4 rectLen=0.05
//				Checked elements: 1000000  vol=0.3906329011241997
//				Overlapping elements: 7133248  vol=0.17652305925406842
//				Ratio: 0.22594494568436752  nSingle=3566624
//x				Ratio: 0.08826152962703421  nSingle=3566624
		
//		k=4 rectLen=0.08
//				Checked elements: 1000000  vol=2.560051780807515
//				Overlapping elements: 50733333  vol=8.30865168090053
//				Ratio: 1.6227507082453891  nSingle=25366666
//x				Ratio: 4.154325840450265  nSingle=25366666
				
//		k=4 rectLen=0.1
//				Checked elements: 1000000  vol=6.250126417987243
//				Overlapping elements: 130904176  vol=52.69602705325548
//				Ratio: 4.2155968958965016  nSingle=65452088
//				Ratio: 26.34801352662774  nSingle=65452088

//		k=5 rectLen=0.01
//				Checked elements: 1000000  vol=3.120910995674637E-6
//				Overlapping elements: 92  vol=7.791567558111558E-12
//				Ratio: 1.2482841658910046E-6  nSingle=46
//				Ratio: 3.895783779055779E-12  nSingle=46
		
//		k=5 rectLen=0.05
//				Checked elements: 1000000  vol=0.009752846861483424
//				Overlapping elements: 370146  vol=1.1493905732292053E-4
//				Ratio: 0.005892590079356486  nSingle=185073
//x				Ratio: 5.7469528661460266E-5  nSingle=185073
		
//		k=5 rectLen=0.06
//				Checked elements: 1000000  vol=0.02426820390236688
//				Overlapping elements: 951756  vol=7.397387678770777E-4
//				Ratio: 0.01524090474212908  nSingle=475878
//				Ratio: 3.6986938393853883E-4  nSingle=475878
				
//		k=5 rectLen=0.08
//				Checked elements: 1000000  vol=0.10226601150626731
//				Overlapping elements: 4290713  vol=0.014161240196383653
//				Ratio: 0.06923727633357339  nSingle=2145356
//				Ratio: 0.007080620098191826  nSingle=2145356
		
//		k=5 rectLen=0.1
//				Checked elements: 1000000  vol=0.3120910995674712
//				Overlapping elements: 14005327  vol=0.1422098937178865
//				Ratio: 0.2278339464261813  nSingle=7002663
//x				Ratio: 0.07110494685894325  nSingle=7002663

//		k=6 rectLen=0.03
//				Checked elements: 1000000  vol=1.139762075593237E-5
//				Overlapping elements: 854  vol=1.4590064461928473E-10
//				Ratio: 6.400486897379202E-6  nSingle=427
//				Ratio: 7.295032230964237E-11  nSingle=427

//		k=6 rectLen=0.04
//				Checked elements: 1000000  vol=6.403930674389243E-5
//				Overlapping elements: 4924  vol=4.731028607269581E-9
//				Ratio: 3.693847456992334E-5  nSingle=2462
//				Ratio: 2.3655143036347906E-9  nSingle=2462

//		k=6 rectLen=0.05
//				Checked elements: 1000000  vol=2.442905683284552E-4
//				Overlapping elements: 19372  vol=7.305864743557814E-8
//				Ratio: 1.4953227203055346E-4  nSingle=9686
//				Ratio: 3.652932371778907E-8  nSingle=9686
				
//		k=6 rectLen=0.08
//				Checked elements: 1000000  vol=0.004098515631609189
//				Overlapping elements: 363616  vol=2.407650703707947E-5
//				Ratio: 0.002937222790050257  nSingle=581808
//				Ratio: 1.2038253518539734E-5  nSingle=581808
	
//		k=6 rectLen=0.1
//				Checked elements: 1000000  vol=0.015634596373020965
//				Overlapping elements: 1500753  vol=3.8434924134137796E-4
//				Ratio: 0.012291626600754798  nSingle=750376
//				Ratio: 1.9217462067068898E-4  nSingle=750376

//		k=6 rectLen=0.15
//				Checked elements: 1000000  vol=0.17808782431144204
//				Overlapping elements: 20983035  vol=0.06296258279236117
//				Ratio: 0.1767739682255073  nSingle=10491517
//x				Ratio: 0.03148129139618058  nSingle=10491517
				
//		k=8 rectLen=0.1
//				Checked elements: 1000000  vol=3.91238964304921E-5
//				Overlapping elements: 17086  vol=2.5197288850638793E-9
//				Ratio: 3.220191641111788E-5  nSingle=8543
//				Ratio: 1.2598644425319396E-9  nSingle=8543
		
//		k=8 rectLen=0.2
//				Checked elements: 1000000  vol=0.010015717486206208
//				Overlapping elements: 7604179  vol=3.443461045122709E-4
//				Ratio: 0.0171902864166501  nSingle=3802089
//				Ratio: 1.7217305225613545E-4  nSingle=3802089
		
//		k=10 rectLen=0.1
//				Checked elements: 1000000  vol=9.787375930845904E-8
//				Overlapping elements: 202  vol=2.3017500990954988E-14
//				Ratio: 1.1758770253430752E-7  nSingle=101
//				Ratio: 1.1508750495477494E-14  nSingle=101
		
		s0.setSeed(0);
		TestRunnerOV test = new TestRunnerOV(s0);
		test.run(r);
	}
	

	private TestRunnerOV(TestStats S) { 
		this.S = S;
		this.R = new Random(S.seed);
	}
	
	private TestStats run(Result r) {
		//load
		resetR();
		load(S);
		
		r.nOverlap = calcOverlap();
		
		return S;
	} 
	
	private void resetR() {
		R.setSeed(S.seed);
	}
	
	private void load(TestStats ts) {
		log(time() + "generating data ...");
		long t1g = System.currentTimeMillis();

		if (ts.isRangeData) {
			test = TestRectangle.create(R, ts);
		} else {
			test = TestPoint.create(R, ts);
		}
		
		switch (ts.TEST) {
		case CUBE:
		case CLUSTER:
		case CSV:
		case OSM:
		case TIGER:
		case TOUCH:
		case VORTEX: {
			data = test.generate();
			break;
		}
		//case ASPECT:
		case MBR_SIZE: {
			//IS_POINT_DATA = PR_TestSize.generate(R, cfgDataLen, N, DIM, 0.001f);
			//IS_POINT_DATA = PR_TestSize.generate(R, cfgDataLen, N, DIM, 0.02f);
			//data = PR_TestAspect.generate(R, cfgDataLen, N, DIM, 1e3f);//10.0f);
			data = test.generate();
			if (!ts.isRangeData) throw new IllegalStateException();
			break;
		}
		default:
			throw new UnsupportedOperationException("No data for: " + ts.TEST.name());
		}
		long t2g = System.currentTimeMillis();
		log("data generation finished in: " + (t2g-t1g));
		//S.statTGen = t2g-t1g;
		
		int dims = S.cfgNDims;
		int N = S.cfgNEntries;
		
		//load index
		log(time() + "loading index ...");
		
		long t1 = System.currentTimeMillis();

		tree = ts.createTree();
		tree.load(data, dims);

		long t2 = System.currentTimeMillis();
		log("loading finished in: " + (t2-t1));
		
		//This avoid premature garbage collection...
		log("loaded objects: " + N + " " + data[0]);
	}
		
	
	static void log(String string) {
		System.out.println(string);
	}


	
	public TestStats getTestStats() {
		return S;
	}
	
	private String time() {
		return FT.format(new Date()) + " ";
	}

	public Candidate getCandidate() {
		return tree;
	}
	
	private int calcOverlap() {
		PhTreeSolidF<Object> pht = ((RectanglePHCF)tree).getNative();
		PhIteratorSF<Object> it = pht.iterator();
		double[] lower = new double[S.cfgNDims];
		double[] upper = new double[S.cfgNDims];
		PhQuerySF<Object> q = pht.queryIntersect(lower, upper);
		int nChecked = 0;
		int nOverlap = 0;
		double dOverlap = 0;
		double dVolume = 0;
		int nSkip = 1;
		int nMaxQueries = 10*1000;
		//This allows us to get a representative sample while still
		//skipping many elements.
		if (pht.size() > nMaxQueries) {
			nSkip = pht.size()/nMaxQueries; 
		}
		int n = 0;
		while (it.hasNext()) {
			if (n++ % nSkip != 0) {
				continue;
			}
			nChecked++;
			PhEntrySF<Object> e = it.nextEntryReuse();
			q.reset(e.lower(), e.upper());
			double myVolume = calcVolume(e);
			dVolume += myVolume;
			while (q.hasNext()) {
				PhEntrySF<Object> e2 = q.nextEntryReuse();
				double myOverlap = calcOverlap(e, e2);
				//check if we have the original box here
				if (myOverlap < myVolume) {
					nOverlap++;
					dOverlap += myOverlap;
				}
			}
			if (nChecked >= 10000) {
				nOverlap = (int) (pht.size()/(double)nChecked * nOverlap);
				System.out.println("Breaking after 10.000: -> Estimate");
				break;
			}
		}
		System.out.println("k=" + S.cfgNDims + " rectLen=" + S.cfgRectLen);
		System.out.println("Checked elements: " + nChecked + "  vol=" + dVolume);
		System.out.println("Overlapping elements: " + nOverlap + "  vol=" + dOverlap);
		System.out.println("Ratio: " + (dOverlap/2/dVolume) + "  nSingle=" + nOverlap/2);
		System.out.println("Ratio: " + (dOverlap/2/1) + "  nSingle=" + nOverlap/2);
		return nOverlap/2;
	}
	
	private static double calcOverlap(PhEntrySF<?> e1, PhEntrySF<?> e2) {
		double[] min1 = e1.lower();
		double[] max1 = e1.upper();
		double[] min2 = e2.lower();
		double[] max2 = e2.upper();
		double area = 1;
		for (int i = 0; i < e1.lower().length; i++) {
			double d = Math.min(max1[i], max2[i]) - Math.max(min1[i], min2[i]);
			if (d <= 0) {
				return 0;
			}
			area *= d;
		}
		return area;
	}
	
	public static double calcVolume(PhEntrySF<?> e) {
		double[] min = e.lower();
		double[] max = e.upper();
		double v = 1;
		for (int d = 0; d < min.length; d++) {
			v *= max[d] - min[d];
		}
		return v;
	}

}
