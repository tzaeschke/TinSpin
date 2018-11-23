/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

class HDF5BlockGCOL extends HDF5Block {
	
	static class GlobalHeapObject {
		public static final long SIZE = 16+16;
		//Heap Object Index / 2
		int s0Index;
		//Reference Count / 2
		int s2ReferenceCount;
		//Reserved (zero) / 4
		long i4Zero;

		//Object SizeL  / L
		long i8ObjectSize;

		//Object Data
		byte[] data;
		
		
		@Override
		public String toString() {
			String ret =  
					//Heap Object Index / 2
					"HeapObjectIndex=" + s0Index + Reader.L +

					//Reference Count / 2
					"ReferenceCount=" + s2ReferenceCount + Reader.L +

					//Reserved (zero) / 4
					//long i4Zero;

					//Object SizeL  / L
					"ObjectSize=" + i8ObjectSize;
			return ret;
		}
	}
	
	static class GlobalHeapID {
		//Collection AddressO
		long i0CollectionAddressesO;

		//Object Index
		long i4ObjectIndex;
		
		@Override
		public String toString() {
			String ret =  
					//Collection AddressO
					"CollectionAddressesO=" + i0CollectionAddressesO + Reader.L +

					//Object Index
					"ObjectIndex=" + i4ObjectIndex + Reader.L;
			return ret;
		}
	}
	
	
	//Signature == GCOL
	int i0Signature;
	//Version
	//int b4Version;
	//Reserved (zero)
	int b5Zero;
	int b6Zero;
	int b7Zero;

	//Collection SizeL
	long i8CollectionSizeL;

//	Global Heap Object 1
//	Global Heap Object 2
//	...
//	Global Heap Object N
//	Global Heap Object 0 (free space)
	GlobalHeapObject[] objects;

	public HDF5BlockGCOL(int offset, int version) {
		super(offset, version);
	}
	
	@Override
	public String toString() {
		return "GCOL(" + getOffset() + ")" + Reader.L + 
//				//Signature
//				"Signature=" + i0Signature + Reader.L +
				//Version
				"Version=" + getVersion() + Reader.L +
				//Reserved (zero)
				//"" + b5Zero + L +

				//Collection SizeL
				"CollectionSizeL=" + i8CollectionSizeL;// + Reader.L;
	}
}