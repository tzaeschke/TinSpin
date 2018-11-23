/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

/**
 * 0x0000
 * NIL message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg0000 extends DOMsg {
	
	
	public DOMsg0000(int offset) {
		super(offset, Reader.MSG.MSG_0000_NIL);
	}
	
	
	@Override
	public String toString() {
		return "0000:NIL: " + super.toString();
	}

}
