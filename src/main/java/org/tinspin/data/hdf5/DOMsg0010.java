/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

/**
 * 0x0010
 * Continuation Message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0010 extends DOMsg {
	
	//This value is the address in the file where the header continuation block is located.
	long offsetO;
	
	//This value is the length in bytes of the header continuation block in the file.	
	long lengthL;
		
	
	public DOMsg0010(int offset) {
		super(offset, Reader.MSG.MSG_0010_CONTINUATION);
	}
	
	
	@Override
	public String toString() {
		return "0010:ContinuationMessage: " + super.toString() + Reader.L +  
				"offset=" + offsetO + Reader.L +
				"length=" + lengthL;
	}
}
