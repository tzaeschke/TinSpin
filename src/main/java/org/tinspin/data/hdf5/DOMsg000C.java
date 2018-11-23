/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

/**
 * 0x000C
 * Attribute message.
 * 
 * @author Tilmann Zäschke
 *
 */
public class DOMsg000C extends DOMsg {

	

	//The version number information is used for changes in the format of the 
	//attribute message and is described here:
	//Version 	Description
	//0 	Never used.
	//1 	Used by the library before version 1.6 to encode attribute message. 
	//      This version does not support shared datatypes.
	int b0Version;

	byte b1Zero;

	//The length of the attribute name in bytes including the null terminator. 
	//Note that the Name field below may contain additional padding not 
	//represented by this field.
	//Name Size
	int s2NameSize;

	//The length of the datatype description in the Datatype field below. 
	//Note that the Datatype field may contain additional padding not 
	//represented by this field.
	//Datatype Size
	int s4DatatypeSize;

	//The length of the dataspace description in the Dataspace field below. 
	//Note that the Dataspace field may contain additional padding not 
	//represented by this field.
	//Dataspace Size
	int s6DataspaceSize;

	//The null-terminated attribute name. This field is padded with additional 
	//null characters to make it a multiple of eight bytes.
	//Name
	String name;

	//The datatype description follows the same format as described for the 
	//datatype object header message. This field is padded with additional 
	//zero bytes to make it a multiple of eight bytes.
	//Datatype
	DOMsg0003 datatype;
		

	//The dataspace description follows the same format as described for the 
	//dataspace object header message. This field is padded with additional 
	//zero bytes to make it a multiple of eight bytes.
	//Dataspace
	DOMsg0001 dataspace;

	//The raw data for the attribute. The size is determined from the datatype 
	//and dataspace descriptions. This field is not padded with additional bytes.
	//Data
	byte[] data;
	
	
	public DOMsg000C(int offset, int version) {
		super(offset, Reader.MSG.MSG_000C_ATTRIBUTE, version);
	}


	@Override
	public String toString() {
		return "000C:AttributeMessage: " + super.toString() + Reader.L +  
				"Version=" + b0Version + Reader.L +
				//"Zero=" + b1Zero + Reader.L +
				"NameSize=" + s2NameSize + Reader.L +
				"DatatypeSize=" + s4DatatypeSize + Reader.L +
				"DataspaceSize=" + s6DataspaceSize + Reader.L +
				"Name=" + name + 
				Reader.NL + datatype +
				Reader.NL + dataspace;
	}

}
