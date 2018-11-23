/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import java.util.Arrays;

/**
 * 0x0008
 * Data Layout message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0008v3 extends DOMsg {

	//Version
	int b0Version;
	//Layout Class 	
	int b1LayoutClass;
	//Reserved (zero)
	int s2Zero;
	
	//Compact Storage
	int s4Size;
	byte[] data;
	
	//contiguous storage
	long l4DataAddressO;
	long l8DataSizeL;
	
	//chunked storage
	int b4Dimensionality;
	long l8DataAddressO;
	int[] dimensions;
	int iDatasetElementSize;
	
	public DOMsg0008v3(int offset, int version) {
		super(offset, Reader.MSG.MSG_0008_DATA_LAYOUT, version);
	}
	

	@Override
	public String toString() {
		String layout;
		switch (b1LayoutClass) {
		case 0:
			layout = "Size=" + s4Size + Reader.L +
			"Data=" + Arrays.toString(data) + Reader.L;
			break;
		case 1:
			layout = "DataAddress=" + l4DataAddressO + Reader.L +
			"DataSize=" + l8DataSizeL + Reader.L;
			break;
		case 2:
			layout = "Dimensionality=" + b4Dimensionality + Reader.L +
			"DataAddress=" + l8DataAddressO + Reader.L +
			"Dimensions=" + Arrays.toString(dimensions) + Reader.L +
			"DataElementSize=" + iDatasetElementSize + Reader.L;
			break;
		default: 
			throw new IllegalArgumentException();
		}
		return "0008:DataLayoutMessage: " + super.toString() + Reader.L +  
				"Version=" + b0Version + Reader.L +
				"LayoutClass=" + b1LayoutClass + Reader.L +
				layout;
//				//"b3Zero=" + b3Zero + Reader.L +
//				//"b4Zero=" + b4Zero + Reader.L +
//				"DataAddressO=" + l8DataAddressO;
	}

}
