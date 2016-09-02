/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data.tiger;

import org.zoodb.jdo.spi.PersistenceCapableImpl;

public class PersistentArrayLong extends PersistenceCapableImpl {

	private long[] data;
	
	@SuppressWarnings("unused")
	private PersistentArrayLong() {
		// for ZooDB
	}
	
	public PersistentArrayLong(int len) {
		data = new long[len];
	}
	
	public long[] getData() {
		zooActivateRead();
		return data;
	}
	
	public void setData(long[] data) {
		zooActivateWrite();
		this.data = data;
	}
	
}
