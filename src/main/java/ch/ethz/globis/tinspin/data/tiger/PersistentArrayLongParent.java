/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data.tiger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zoodb.jdo.spi.PersistenceCapableImpl;

public class PersistentArrayLongParent extends PersistenceCapableImpl {

	public static final int CHUNK_SIZE = 100000;
	
	private long totalEntryCount;
	private int DIM;
	private ArrayList<PersistentArrayLong> data;
	
	@SuppressWarnings("unused")
	private PersistentArrayLongParent() {
		// for ZooDB
	}
	
	public PersistentArrayLongParent(int totalEntryCount, int DIM) {
		this.totalEntryCount = totalEntryCount;
		this.DIM = DIM;
		data = new ArrayList<PersistentArrayLong>();
	}
	
	public PersistentArrayLong getNextForWrite() {
		zooActivateWrite();
		int len;
		if ((data.size()+1) * CHUNK_SIZE < totalEntryCount) {
			len = CHUNK_SIZE;
		} else {
			len = (int) (totalEntryCount - CHUNK_SIZE * data.size());
		}
		PersistentArrayLong ret = new PersistentArrayLong(len * DIM);
		data.add(ret);
		return ret;
	}
	
	public List<PersistentArrayLong> getData() {
		zooActivateRead();
		return Collections.unmodifiableList(data);
	}
	
	public long getEntryCount() {
		zooActivateRead();
		return totalEntryCount;
	}

	public int getDim() {
		zooActivateRead();
		return DIM;
	}
	
}
