package org.zoodb.index.quadtree;

import java.util.Arrays;

public class QREntry<T> {

	private double[] pointL;
	private double[] pointU;
	private final T value;
	
	public QREntry(double[] keyL, double[] keyU, T value) {
		this.pointL = keyL;
		this.pointU = keyU;
		this.value = value;
	}
	
	public double[] getPointL() {
		return pointL;
	}
	
	public double[] getPointU() {
		return pointU;
	}
	
	public T getValue() {
		return value;
	}

	public boolean enclosedByXX(double[] min, double[] max) {
		return QUtil.isRectEnclosed(this.pointL, this.pointU, min, max);
	}

	public boolean isExact(QREntry<T> e) {
		return QUtil.isPointEqual(pointL, e.getPointL()) 
				&& QUtil.isPointEqual(pointU, e.getPointU());
	}

	@Override
	public String toString() {
		return "p=" + Arrays.toString(pointL) + "/" + Arrays.toString(pointU) + 
				"  v=" + value + " " + System.identityHashCode(this);
	}

	public void setKey(double[] newPointL, double[] newPointU) {
		this.pointL = newPointL;
		this.pointU = newPointU;
	}

}
