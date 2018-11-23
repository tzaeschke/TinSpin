/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

/**
 * 0x0011
 * Symbol Table Message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0011 extends DOMsg {
	
	//v1 B-tree Address
	//This value is the address of the v1 B-tree containing the symbol table entries for the group.
	long l0V1BTreeAddressO;

	//Local Heap Address
	//This value is the address of the local heap containing the link names for the symbol table entries for the group.
	long l8LocalHeapAddressO;
		
	
	public DOMsg0011(int offset) {
		super(offset, Reader.MSG.MSG_0011_SYMBOL_TABLE);
	}
	
	
	@Override
	public String toString() {
		return "0011:SymbolTableMessage: " + super.toString() + Reader.L +  
				"V1BTreeAddressO=" + l0V1BTreeAddressO + Reader.L +
				"LocalHeapAddressO=" + l8LocalHeapAddressO;
	}
}
