/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

public class HDF5Block {

	private final int offset;
	private final int version;

	public HDF5Block(int offset, int version) {
		this.offset = offset;
		this.version = version;
	}

	int getOffset() {
		return offset;
	}

	int getVersion() {
		return version;
	}
	
	public void assertVersion(int version) {
		if (version != getVersion()) {
			throw new IllegalStateException("Illegal block version, expected " +
					version + ", got " + getVersion());
		}
	}
}
