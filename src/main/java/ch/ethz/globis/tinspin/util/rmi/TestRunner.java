/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util.rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Permission;
import java.util.Date;

import ch.ethz.globis.phtree.util.Tools;
import ch.ethz.globis.tinspin.MainTest;
import ch.ethz.globis.tinspin.TestStats;

public class TestRunner implements TestRunnerAPI {

	static class NoSecurityManager extends SecurityManager {
	    @Override
		public void checkConnect (String host, int port) {}
	    @Override
		public void checkConnect (String host, int port, Object context) {}
	    @Override
		public void checkPropertyAccess(String key) {};
	    @Override
		public void checkPermission(Permission perm) {};
	    @Override
		public void checkPermission(Permission perm, Object context) {};
	    @Override
		public void checkAccept(String host, int port) {};   
	}
	
	@Override
	public TestStats executeTask(TestStats stats0) {
        return test(stats0);
    }
	
	public static void main(String[] args) {
		System.out.println("TestRunner: started: " + args[0] + " " + args[1] + " " + args[2]);
		//System.out.println("TestRunner time: " + new Date());
		
		System.setSecurityManager (new NoSecurityManager());
        try {
            TestRunner engine = new TestRunner();
            TestRunnerAPI stub =
                (TestRunnerAPI) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(TestManagerRMI.RMI_NAME, stub);
            //System.out.println("TestRunner bound");
        } catch (Exception e) {
            System.err.println("TestRunner exception:");
            e.printStackTrace();
        }
		
		//System.out.println("TestRunner finished: " + args[0]);
	}
	
	static TestStats test(TestStats stats) {
		//warm-up
		//testWarm(TEST, INDEX, DIM, DEPTH, 100*1000, 0, param1);
		TestStats s2 = stats.cloneStats();
		s2.setN(100*1000);
		s2.setSeed(0);
		testWarm(s2);
		//warm test
		return testWarm(stats);
	}
	
	private static TestStats testWarm(TestStats stats) {
		//clean-up
        long memTree = Tools.getMemUsed();
		Tools.cleanMem(stats.getN(), memTree);

		System.out.println("Testrunner: starting task: " + new Date());
		TestStats s;
		MainTest test = new MainTest(stats);
		try {
			s = test.run();
		} catch (Throwable t) {
			s = test.getTestStats();
			s.setFailed(t);
		}
		System.out.println("Testrunner: Task finished: " + new Date());
		return s;
	}

	
}
