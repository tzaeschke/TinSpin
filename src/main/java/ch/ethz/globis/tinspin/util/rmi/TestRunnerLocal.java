/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util.rmi;

import java.rmi.NoSuchObjectException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Permission;
import java.util.Date;

import ch.ethz.globis.phtree.util.Tools;
import ch.ethz.globis.tinspin.TestRunner;
import ch.ethz.globis.tinspin.TestStats;

public class TestRunnerLocal implements TestRunnerAPI {

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
		try {
			return test(stats0);
		} finally {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException e) {
				e.printStackTrace();
			}
		}
    }
	
	@Override
	public boolean isAlive() {
		return true;
	}
	
	public static void main(String[] args) {
		System.out.println("TestRunnerLocal: started: " + args[0] + " " + args[1] + " " + args[2]);
		//System.out.println("TestRunnerLocal time: " + new Date());
		
		System.setSecurityManager (new NoSecurityManager());
        try {
            TestRunnerLocal engine = new TestRunnerLocal();
            TestRunnerAPI stub =
                (TestRunnerAPI) UnicastRemoteObject.exportObject(engine, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(TestManagerRMI.RMI_NAME, stub);
            //System.out.println("TestRunnerLocal bound");
        } catch (Exception e) {
            System.err.println("TestRunnerLocal exception:");
            e.printStackTrace();
        }
		
		//System.out.println("TestRunnerLocal finished: " + args[0]);
	}
	
	static TestStats test(TestStats stats) {
		//warm-up
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
		TestRunner test = new TestRunner(stats);
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
