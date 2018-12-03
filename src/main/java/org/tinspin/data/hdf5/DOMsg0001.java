/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import java.util.Arrays;

/**
 * 0x0001
 * Dataspace message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0001 extends DOMsg {
	
	//This value is used to determine the format of the Dataspace Message. When the format of the information in the message is changed, the version number is incremented and can be used to determine how the information in the object header is formatted. This document describes version one (1) (there was no version zero (0)).
	int b0Version;

	//This value is the number of dimensions that the data object has.
	int b1Dimensionality;
	
	//This field is used to store flags to indicate the presence of parts of this message. Bit 0 (the least significant bit) is used to indicate that maximum dimensions are present. Bit 1 is used to indicate that permutation indices are present.
	int b2Flags;
	
	byte b3Zero;
	int i4Zero;

	//Entry[] data;
	long[] dataDim;
	long[] dataDimMax;
	
	public DOMsg0001(int offset, int version) {
		super(offset, Reader.MSG.MSG_0001_DATA_SPACE, version);
	}
	
	
	@Override
	public String toString() {
		return "0001:DataspaceMessage: " + super.toString() + Reader.L +  
				"Version=" + b0Version + Reader.L +
				"Dimensionality=" + b1Dimensionality + Reader.L +
				"Flags=" + b2Flags + Reader.L +
				"dataDim=" + Arrays.toString(dataDim) + Reader.L +
				"dataDimMax=" + Arrays.toString(dataDimMax);
	}
	
	static class Entry {
		/**
		 * This value is the current size of the dimension of the data as stored in the 
		 * file. The first dimension stored in the list of dimensions is the slowest changing 
		 * dimension and the last dimension stored is the fastest changing dimension.
		 */
		long l0DimensionSize;// #n 

		/** 
		 * This value is the maximum size of the dimension of the data as stored in the file. 
		 * This value may be the special "unlimited" size which indicates that the data may expand
		 * along this dimension indefinitely. If these values are not stored, the maximum size of 
		 * each dimension is assumed to be the dimension’s current size.
		 */
		long l8DimensionMaximumSize;// #n

		/**
		 * This value is the index permutation used to map each dimension from the canonical 
		 * representation to an alternate axis for each dimension. If these values are not stored,
		 * the first dimension stored in the list of dimensions is the slowest changing dimension 
		 * and the last dimension stored is the fastest changing dimension. 
		 */
		long l16PermutationIndex;// #n
	}

}
