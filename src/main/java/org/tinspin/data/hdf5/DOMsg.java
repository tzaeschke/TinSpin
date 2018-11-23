/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import java.util.Arrays;

public class DOMsg extends HDF5Block {
	
	//Header Message Type #1 /2
	Reader.MSG s12HeaderMsgType;
	int s12HeaderMsgTypeId;
	
	//This value specifies the number of bytes of header message data following 
	//the header message type and length information for the current message. 
	//The size includes padding bytes to make the message a multiple of eight bytes. 
	//Size of Header Message Data #1 / 2
	int s14SizeHeaderMsgData;
	
	//Header Message #1 Flags / 1
	int b16HeaderMsgFlags;
	
	//Reserved (zero) /3
	byte b17Zero;
	byte b18Zero;
	byte b19Zero;
	
	//Header Message Data #1
	byte[] b20data;
	
	
	static DOMsg create(Reader.MSG type, int pos, byte version) {
		switch (type) {
		case MSG_0000_NIL:
			return new DOMsg0000(pos);
		case MSG_0001_DATA_SPACE:
			if (version == 1) {
				return new DOMsg0001(pos, version);
			}
			new IllegalArgumentException("Block 0x0001 version " + version).printStackTrace();
			return new DOMsg0001(pos, version);
//TODO			break;
		case MSG_0003_DATA_TYPE:
			version >>>= 4; //Top 4 bits
			if (version == 1) {
				return new DOMsg0003(pos, version);
			}
			break;
		case MSG_0005_FILL_VALUE:
			if (version == 1 || version == 2) {
				return new DOMsg0005(pos, version);
			}
			break;
		case MSG_0008_DATA_LAYOUT:
			if (version == 121212) {
				return new DOMsg0008(pos, version);
			}
			if (version == 3) {
				return new DOMsg0008v3(pos, version);
			}
			new IllegalArgumentException("Block 0x0008 version " + version).printStackTrace();
			return new DOMsg0008(pos, version);
		case MSG_000C_ATTRIBUTE:
			if (version == 1) {
				return new DOMsg000C(pos, version);
			}
		case MSG_0010_CONTINUATION:
			return new DOMsg0010(pos);
		case MSG_0011_SYMBOL_TABLE:
			return new DOMsg0011(pos);
		case MSG_0012_OBJ_MOD_TIME:
			if (version == 1) {
				return new DOMsg0012(pos, version);
			}
			break;
		default:
			throw new UnsupportedOperationException("Message type: 0x" + 
					Integer.toHexString(type.type()) + " version " + version);
		}
		throw new UnsupportedOperationException("Illegal block version " +
				version + " for block type 0x" + Integer.toHexString(type.type()));
	}
	
	
	public DOMsg(int offset, Reader.MSG type) {
		this(offset, type, Reader.NO_VERSION);
	}
	
	
	public DOMsg(int offset, Reader.MSG type, int version) {
		super(offset, version);
		this.s12HeaderMsgType = type;
		this.s12HeaderMsgTypeId = type.type();
	}
	
	
	@Override
	public String toString() {
		return "DOMsg(" + getOffset() + ")" + Reader.L + 
				"HeaderMsgType=0x" + Integer.toHexString(s12HeaderMsgType.type()) + Reader.L +
				"SizeHeaderMsgData=" + s14SizeHeaderMsgData + Reader.L +
				"HeaderMsgFlags=0b" + Integer.toBinaryString(b16HeaderMsgFlags) + Reader.L + 
				"data=" + Arrays.toString(b20data);// + Reader.L +

				//Reserved (zero)
//				"" + b17Zero + Reader.L +
//				"" + b18Zero + Reader.L +
//				"" + b19Zero + Reader.L
	}
	

}
