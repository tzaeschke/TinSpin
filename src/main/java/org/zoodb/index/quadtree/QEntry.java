package org.zoodb.index.quadtree;

import java.util.Arrays;

public class QEntry<T> {

	private double[] point;
	private final T value;
	
	public QEntry(double[] key, T value) {
		this.point = key;
		this.value = value;
	}
	
	public double[] getPoint() {
		return point;
	}
	
	public T getValue() {
		return value;
	}

	public boolean enclosedBy(double[] min, double[] max) {
		return QUtil.isPointEnclosed(point, min, max);
	}

	public boolean isExact(QEntry<T> e) {
		return QUtil.isPointEqual(point, e.getPoint());
	}

	@Override
	public String toString() {
		return "p=" + Arrays.toString(point) + "  v=" + value + " " + 
				System.identityHashCode(this);
	}

	public void setKey(double[] newPoint) {
		this.point = newPoint;
	}

}
