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

public class PersistentArrayDoubleParent extends PersistenceCapableImpl {

	public static final int CHUNK_SIZE = 100000;
	
	private int totalEntryCount;
	private int dims;
	private ArrayList<PersistentArrayDouble> data;
	
	@SuppressWarnings("unused")
	private PersistentArrayDoubleParent() {
		// for ZooDB
	}
	
	public PersistentArrayDoubleParent(int totalEntryCount, int dims) {
		this.totalEntryCount = totalEntryCount;
		this.dims = dims;
		data = new ArrayList<PersistentArrayDouble>();
	}
	
	public PersistentArrayDouble getNextForWrite() {
		zooActivateWrite();
		int len;
		if ((data.size()+1) * CHUNK_SIZE < totalEntryCount) {
			len = CHUNK_SIZE;
		} else {
			len = totalEntryCount - CHUNK_SIZE * data.size();
		}
		PersistentArrayDouble ret = new PersistentArrayDouble(len * dims);
		data.add(ret);
		return ret;
	}
	
	public List<PersistentArrayDouble> getData() {
		zooActivateRead();
		return Collections.unmodifiableList(data);
	}
	
	public int getEntryCount() {
		zooActivateRead();
		return totalEntryCount;
	}

	public int getDim() {
		zooActivateRead();
		return dims;
	}
	
}
