/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

/**
 * 0x0005
 * Fill Value message (default value for uninitialized fields).
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0005 extends DOMsg {

	//Version
	int b0Version;
	//Space Allocation Time
	int b1SpacAllocTime;
	//Fill Value Write Time
	int b2FillValueWriteTime;
	//Fill Value Defined
	int b3FillValueDefined;
	//Size (optional)
	int i4Size;
	//Fill Value (optional, variable size)
	long l8FillValue;
	
	
	public DOMsg0005(int offset, int version) {
		super(offset, Reader.MSG.MSG_0005_FILL_VALUE, version);
	}
	
	
	@Override
	public String toString() {
		return "0005:FillValueMessage: " + super.toString() + Reader.L +  
				"Version=" + b0Version + Reader.L +
				"SpacAllocTime=" + b1SpacAllocTime + Reader.L +
				"FillValueWriteTime=" + b2FillValueWriteTime + Reader.L +
				"FillValueDefined=" + b3FillValueDefined + Reader.L +
				"Size=" + i4Size + Reader.L +
				"FillValue=" + l8FillValue;
	}

}
