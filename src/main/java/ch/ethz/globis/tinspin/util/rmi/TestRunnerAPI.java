/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.ethz.globis.tinspin.TestStats;

public interface TestRunnerAPI extends Remote {
    TestStats executeTask(TestStats stats0) 
    		throws RemoteException;
    boolean isAlive()
    		throws RemoteException;

}
