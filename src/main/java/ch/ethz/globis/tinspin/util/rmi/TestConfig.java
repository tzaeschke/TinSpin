/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.util.rmi;

import java.io.Serializable;

public class TestConfig implements Serializable {

	/** */
	private static final long serialVersionUID = 1L;
	private int k;
	
	@SuppressWarnings("unused")
	public TestConfig(int i, int j, int k) {
		this.k = k;
	}

	public String execute() {
		System.out.println("Hey-ho, mey task");
		return "This is a perfect result: " + k;
	}

}
