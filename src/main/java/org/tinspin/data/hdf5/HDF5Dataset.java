/*
 * Copyright 2018 Tilmann Zäschke. All Rights Reserved.
 *
 * This software is the proprietary information of Tilmann Zäschke.
 * Use is subject to license terms.
 */
package org.tinspin.data.hdf5;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class HDF5Dataset {
	
	private final Reader reader;
	private DOHeaderPrefix dataset;
	
	//The dataset nam. This is not an official HDF5 attribute.
	private String datasetName;
	
	HDF5Dataset(Reader reader, String name, DOHeaderPrefix dataset) {
		this.reader = reader;
		this.datasetName = name;
		this.dataset = dataset;
	}
	
	/**
	 * This is NOT an official HDF5 method!
	 * @param datasetName The name of the Dataset. 
	 */
	public void setName(String datasetName) {
		this.datasetName = datasetName;
	}
	
	public String getName() {
		return this.datasetName;
	}
	
	DOHeaderPrefix getDataset() {
		return dataset;
	}
	
	public int getDims() {
		DOHeaderPrefix dohp = dataset; 
		return (int) ((DOMsg0001)dohp.messages[0]).dataDim[1];
	}
	
	public int getCount() {
		DOHeaderPrefix dohp = dataset; 
		return (int) ((DOMsg0001)dohp.messages[0]).dataDim[0];
	}
	
	public double[] getDatasetAsDoubleArray(int nMax, boolean skipNullVectors) {
		//This is just guesswork
		DOHeaderPrefix dohp = dataset; 
		int count = (int) ((DOMsg0001)dohp.messages[0]).dataDim[0];
		int dims = (int) ((DOMsg0001)dohp.messages[0]).dataDim[1];
		int pos = (int) ((DOMsg0008v3)dohp.messages[3]).l4DataAddressO;
		int nBytes = (int) ((DOMsg0008v3)dohp.messages[3]).l8DataSizeL;
		
		if (nMax > 0) {
			count = Math.min(count, nMax);
		}
		double[] f = new double[count*dims];
		ByteBuffer bb = reader.getByteBuffer();
		bb.position(pos);
		for (int p = 0; p < count*dims; p++) {
			f[p] = bb.getFloat();
		}

		if (skipNullVectors) {
			int nSkipped = 0;
			for (int c = 0, c2 = 0; c < count; c++, c2++) {
				boolean nonZero = false;
				for (int d = 0; d < dims; d++) {
					if (0 != (f[c2*dims+d] = f[c*dims+d])) {
						nonZero = true;
					}
				}
				if (!nonZero) {
					nSkipped++;
					c2--;
				}
			}
			f = Arrays.copyOf(f, dims * (count - nSkipped));
			System.out.println("Count=" + count + "; skipped=" + nSkipped);
		}
		
		//print
//		System.out.println("DOHP: " + dohp);
//		for (int i = 0; i <= 10; i++) {
//			System.out.println(Arrays.toString(Arrays.copyOf(f[i], 300)));
//		}
		return f;
	}

}
