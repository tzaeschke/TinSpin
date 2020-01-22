/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util.rmi;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ch.ethz.globis.tinspin.TestStats;
import ch.ethz.globis.tinspin.util.rmi.TestRunnerLocal.NoSecurityManager;


public class TestManagerRMI {

	public static String PROCESS_OPTIONS = "-Xmx24G -XX:+UseConcMarkSweepGC"; 
    
    private static final boolean USE_RMI = true;
	
	static final String RMI_NAME = "TestRunnerRMI";
	
	
	private static TestStats runRmiTest(TestStats stats0) {
		TestProcessLauncher.launchRmiRegistry();

		//Check whether there are already tests running
		try {
			Registry registry = LocateRegistry.getRegistry();
			TestRunnerAPI comp = (TestRunnerAPI) registry.lookup(RMI_NAME);
			if (comp.isAlive()) {
				throw new IllegalStateException("Test process already running!");
			}
		} catch (NotBoundException e) {
			//good!
		} catch (ConnectException e) {
			//good!
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		// start test process
		System.out.println("Manager: starting task.");
		//TestProcessLauncher.launchProcess("rmigegistry", new String[]{});
		System.setSecurityManager (new NoSecurityManager());
		Process p = TestProcessLauncher.launchProcess(PROCESS_OPTIONS, 
				TestRunnerLocal.class, new String[]{
			stats0.INDEX.name(), stats0.TEST.name(), Integer.toString(stats0.getN())});
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// run test
		TestStats stats = null;
		try {
			Registry registry = LocateRegistry.getRegistry();
			TestRunnerAPI comp = (TestRunnerAPI) registry.lookup(RMI_NAME);
			stats = comp.executeTask(stats0);
		} catch (Exception e) {
			e.printStackTrace();
			stats = stats0;
			stats.setFailed(e);
		}

		//end process
		p.destroy();
		
		//wait for end
		try {
			p.waitFor();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		return stats;
	}
	
	public static TestStats runTest(TestStats s1) {
		if (USE_RMI) {
			return runRmiTest(s1);
		} else {
			return TestRunnerLocal.test(s1);
		}
	}
	
}
