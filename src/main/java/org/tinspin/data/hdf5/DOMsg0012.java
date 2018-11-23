/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import java.util.Date;

/**
 * 0x0012
 * Object Modification Time Message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0012 extends DOMsg {
	
	//Version
	int b0Version;
	//Reserved (zero)
	byte b1Zero;
	byte b2Zero;
	byte b3Zero;
	
	//Seconds After UNIX Epoch
	long i4SecondEpoch;
		
	
	public DOMsg0012(int offset, int version) {
		super(offset, Reader.MSG.MSG_0012_OBJ_MOD_TIME, version);
	}
	
	
	@Override
	public String toString() {
		return "0012:ObjModTime: " + super.toString() + Reader.L +  
				"Version=" + b0Version + Reader.L +
				"SecondEpoch=" + i4SecondEpoch + "/" + new Date(i4SecondEpoch*1000);
	}
}
