/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

/**
 * 0x0008
 * Data Layout message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0008 extends DOMsg {

	//Version
	int b0Version;
	//Dimensionality
	int b1Dimensionality;
	//Layout Class 	
	int b2LayoutClass;
	//Reserved (zero)
	byte b3Zero;
	//Reserved (zero)
	int i4Zero;
	//Data AddressO (optional)
	long l8DataAddressO;
	
	
	public DOMsg0008(int offset, int version) {
		super(offset, Reader.MSG.MSG_0008_DATA_LAYOUT, version);
	}
	

	@Override
	public String toString() {
		return "0008:DataLayoutMessage: " + super.toString() + Reader.L +  
				"Version=" + b0Version + Reader.L +
				"Dimensionality=" + b1Dimensionality + Reader.L +
				"LayoutClass=" + b2LayoutClass + Reader.L +
				//"b3Zero=" + b3Zero + Reader.L +
				//"b4Zero=" + b4Zero + Reader.L +
				"DataAddressO=" + l8DataAddressO;
	}

}
