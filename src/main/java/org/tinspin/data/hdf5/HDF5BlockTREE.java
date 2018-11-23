/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

class HDF5BlockTREE extends HDF5Block {

	static class HDF5Entry {
		
	}

	//Signature
	int i0Signature;
	//Node Type
	int b4nodeType;
	//Node Level
	int b5nodeLevel;
	//Entries Used
	int s6entriesUsed;
	//Address of Left SiblingO
	long l8addreLeftSiblO;
	//Address of Right SiblingO
	long l16addreRightSiblO;
	
	long[] childPointers;
	Object keys;
	
	HDF5Entry[] entries;
	
	//Key 0 (variable size)
	//Address of Child 0O
	//Key 1 (variable size)
	//Address of Child 1O
	//...
	//Key 2K (variable size)
	//Address of Child 2KO
	//Key 2K+1 (variable size)
	
	public HDF5BlockTREE(int offset, int version) {
		super(offset, version);
	}
	
	@Override
	public String toString() {
		return "TREE(" + getOffset() + ")" + Reader.L + 
//				//Signature
//				"Signature=" + i0Signature + Reader.L +
				//Node Type
				"nodeType=" + b4nodeType + Reader.L +
				//Node Level
				"nodeLevel=" + b5nodeLevel + Reader.L +
				//Entries Used
				"entriesUsed=" + s6entriesUsed + Reader.L +
				//Address of Left SiblingO
				"addreLeftSiblO=" + l8addreLeftSiblO + Reader.L +
				//Address of Right SiblingO
				"addreRightSiblO=" + l16addreRightSiblO;
	}
}