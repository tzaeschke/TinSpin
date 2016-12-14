/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.db;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.zoodb.jdo.ZooJdoProperties;
import org.zoodb.tools.ZooHelper;

public class DbWriter {
	private final String dbName;
	private PersistenceManager pm;
	private PersistentArrayDouble pad;
	private PersistentArrayDoubleParent list; 
	private double[] data;
	private int pos = 0;
	private int n = 0;
	
	public DbWriter(String dbName) {
		this.dbName = dbName;
	}
	
	public void init(int doublesPerRow) {
		System.out.println("Storing to database");
		ZooHelper.getDataStoreManager().createDb(dbName);
		
		ZooJdoProperties prop = new ZooJdoProperties(dbName);
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(prop);
		pmf.setRetainValues(false);
		pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		
		list = new PersistentArrayDoubleParent(doublesPerRow);
		pm.makePersistent(list);
		
		pad = list.getNextForWrite2();
		data = pad.getData();
	}
	
	public void write(double[] v) {
		for (int k = 0; k < v.length; k++) {
			data[pos++] = v[k]; 
		}
		
		writeBuffer();
	}
	
	public void write(double[] v1, double[] v2) {
		for (int k = 0; k < v1.length; k++) {
			data[pos++] = v1[k]; 
		}
		for (int k = 0; k < v2.length; k++) {
			data[pos++] = v2[k]; 
		}
		
		writeBuffer();
	}
	
	private void writeBuffer() {
		printProgress(++n);

		if (n % PersistentArrayDoubleParent.CHUNK_SIZE == 0) {
			pm.currentTransaction().commit();
			pm.currentTransaction().begin();
			pm.evict(pad);
			pad = list.getNextForWrite2();
			data = pad.getData();
			pos = 0;
		}
	}
	
	public void close() {
		list.adjustSize(n);
		
		System.out.println();
		
		System.out.println("comitting...");
		pm.currentTransaction().commit();
		System.out.println("done");
		pm.close();
		pm.getPersistenceManagerFactory().close();
	}


	private void printProgress(int n) {
		if (n % 10000 == 0) {
			System.out.print('.');
			if (n % 1000000 == 0) {
				System.out.println(" " + n);
			}
		}
	}
}