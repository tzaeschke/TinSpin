/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import org.tinspin.data.hdf5.HDF5BlockSNOD.SymbolTableEntry;

/**
 */
class HDF5BlockSBHeader extends HDF5Block {
	//Version # of Superblock
	//int b8sbVersion;
	//Version # of File's Free Space Storage
	int b9ffssVersion;
	//Version # of Root Group Symbol Table Entry 	
	int b10rgsteVersion;
	//Reserved (zero)
	int b11zero;
	//Version Number of Shared Header Message Format
	int b12nshmfVersion;
	//Size of Offsets 	
	int b13sizeOfOffsets;
	//Size of Lengths 	
	int b14sizeOfLength;
	//Reserved (zero)
	int b15zero;
	//b2 Group Leaf Node K
	int s16gLeafNodeK;
	//b2 Group Internal Node K
	int s18gIntNodeK;
	//b4 File Consistency Flags
	int i20fcf;
	//b2 Indexed Storage Internal Node K1
	int s24isIntNodeK;
	//b2 Reserved (zero)1
	int s26zero;
	//b8 Base AddressO
	long l28baseAddrO;
	//b8 Address of File Free space InfoO
	long l36addrFFSIO;
	//b8 End of File AddressO
	long l44eofAddrO;
	//b8 Driver Information Block AddressO
	long l52dibAddrO;
	//b4 Root Group Symbol Table Entry
	//int i60rootGSTE;
	//Root Group Symbol Table Entry
	SymbolTableEntry rootGroupSymbolTableEntry;
	DOHeaderPrefix rootGroupSymbolTableEntryHEADER;
	
	public HDF5BlockSBHeader(int offset, int version) {
		super(offset, version);
	}
	
	@Override
	public String toString() {
		return "HEADER(" + getOffset() + ")" + Reader.L + 
				//Version # of Superblock
				"b8sbVersion=" + getVersion() + Reader.L +
				//Version # of File's Free Space Storage
				"b9ffssVersion=" + b9ffssVersion + Reader.L +
				//Version # of Root Group Symbol Table Entry 	
				"b10rgsteVersion=" + b10rgsteVersion + Reader.L +
				//Reserved (zero)
				//"b11zero=" + b11zero +  L +
				//Version Number of Shared Header Message Format
				"b12nshmfVersion=" + b12nshmfVersion +  Reader.L +
				//Size of Offsets 	
				"b13sizeOfOffsets=" + b13sizeOfOffsets + Reader.L +
				//Size of Lengths 	
				"b14sizeOfLength=" + b14sizeOfLength + Reader.L +
				//Reserved (zero)
				//"b15zero=" + b15zero + L +
				//Group Leaf Node K
				"s16gLeafNodeK=" + s16gLeafNodeK + Reader.L +
				//Group Internal Node K
				"s18gIntNodeK=" + s18gIntNodeK + Reader.L +
				//b4 File Consistency Flags
				//"i20fcf=" + i20fcf + L +
				//b2 Indexed Storage Internal Node K1
				"s24isIntNodeK=" + s24isIntNodeK + Reader.L +
				//b2 Reserved (zero)1
				//"s26zero=" + s26zero + L +
				//b8 Base AddressO
				"l28baseAddrO=" + l28baseAddrO + Reader.L +
				//b8 Address of File Free space InfoO
				"l36addrFFSIO=" + l36addrFFSIO + Reader.L +
				//b8 End of File AddressO
				"l44eofAddrO=" + l44eofAddrO;
				//b8 Driver Information Block AddressO
				//"l52dibAddrO=" + l52dibAddrO + L +
				//b4 Root Group Symbol Table Entry
				//"i60rootGSTE=" + i60rootGSTE;
	}

	public int getGroupLeafNodeK() {
		return s16gLeafNodeK;
	}

	public int getSizeLen() {
		return b14sizeOfLength;
	}

	public int getSizeOffset() {
		return b13sizeOfOffsets;
	}

	public int getL() {
		return getSizeLen();
	}

	public int getO() {
		return getSizeOffset();
	}
}