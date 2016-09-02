/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data.tiger;

import org.zoodb.jdo.spi.PersistenceCapableImpl;

public class PersistentArrayDouble extends PersistenceCapableImpl {

	private double[] data;
	
	@SuppressWarnings("unused")
	private PersistentArrayDouble() {
		// for ZooDB
	}
	
	public PersistentArrayDouble(int len) {
		data = new double[len];
	}
	
	public double[] getData() {
		zooActivateRead();
		return data;
	}
	
	public void setData(double[] data) {
		zooActivateWrite();
		this.data = data;
	}
	
}
