/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

class HDF5BlockSNOD extends HDF5Block {
	
	static class SymbolTableEntry extends HDF5Block {
		static final int SIZE = 40;
		
		//Link Name OffsetO
		long l0LinkNameOffsetO;

		//Object Header AddressO
		int l8ObjectHeaderAddressO;

		//Cache Type
		long i16CachedType;
		
		//Reserved (zero)
		long i20Zero;

		//Scratch-pad Space (16 bytes)
		
		//CT 1: Address of B-treeO
		long l24ct1addressBTreeO;

		//CT 1: Address of Name HeapO
		long l32ct1addressNameHeapO;

		//CT 2: Offset to Link Value (4 bytes)
		long i24ct2offsetToLink;
		
		public SymbolTableEntry(int offset, int version) {
			super(offset, version);
		}
		
		@Override
		public String toString() {
			String ret =  
					"SymTE(" + getOffset() + ")" + Reader.L + 
					//Link Name OffsetO
					"LinkNameOffset=" + l0LinkNameOffsetO + Reader.L +

					//Object Header AddressO
					"ObjectHeaderAddress=" + l8ObjectHeaderAddressO + Reader.L +

					//Cache Type
					"CachedType=" + i16CachedType + Reader.L;

			//Reserved (zero)
			//"Zero=" + i12Zero + Reader.L;

			//Scratch-pad Space (16 bytes)

			if (i16CachedType == 1) {
				ret +=
						//CT 1: Address of B-treeO
						"ct1addressBTreeO=" + l24ct1addressBTreeO + Reader.L + 

						//CT 1: Address of Name HeapO
						"ct1addressNameHeapO=" + l32ct1addressNameHeapO;
			} else if (i16CachedType == 2) {
				ret +=
						//CT 2: Offset to Link Value (4 bytes)
						"ct2offsetToLink=" + i24ct2offsetToLink;
			}
			return ret;
		}

		public int getOffsetTREE() {
			if (i16CachedType == 1) {
				return (int) l24ct1addressBTreeO;
			} else if (i16CachedType == 2) {
//				ret +=
//						//CT 2: Offset to Link Value (4 bytes)
//						"ct2offsetToLink=" + i24ct2offsetToLink;
			}
			throw new UnsupportedOperationException();
		}

		public int getOffsetHEAP() {
			if (i16CachedType == 1) {
				return (int) l32ct1addressNameHeapO;
			} else if (i16CachedType == 2) {
//				ret +=
//						//CT 2: Offset to Link Value (4 bytes)
//						"ct2offsetToLink=" + i24ct2offsetToLink;
			}
			throw new UnsupportedOperationException();
		}
	}
	
	
	
	//Signature == SNOD
	int i0Signature;
	//Version == 1
	//int b4Version;
	//Reserved (zero)
	int b5Zero;

	//Number of (used) entries in the symbol table. other entries are 'undefined'
	int s6NumberOfUsedEntries;

	SymbolTableEntry[] symbols;
	
	HDF5BlockSNOD(int offset, int version) {
		super(offset, version);
	}
	
	@Override
	public String toString() {
		return "SNOD(" + getOffset() + ")" + Reader.L + 
//				//Signature
//				"Signature=" + i0Signature + Reader.L +
				//Version
				"Version=" + getVersion() + Reader.L +
				//Reserved (zero)
				//"" + b5Zero + L +

				//Number of (used) entries in the symbol table. other entries are 'undefined'
				"numberOfEntries=" + s6NumberOfUsedEntries;// + Reader.L;
	}
}