package org.zoodb.index.quadtree;

import java.util.Comparator;

public class QREntryDist<T> extends QREntry<T> {
	private double distance;
	
	public QREntryDist(QREntry<T> e, double dist) {
		super(e.getPointL(), e.getPointU(), e.getValue());
		this.distance = dist;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public static final QEntryComparator COMP = new QEntryComparator();
	
	static class QEntryComparator implements Comparator<QREntryDist<?>> {

	    /**
	    * Compares the two specified MBRs according to
	    * the sorting dimension and the sorting co-ordinate for the dimension
	     * of this Comparator.
	    *
	    * @param o1 the first SpatialPoint
	    * @param o2 the second SpatialPoint
	    * @return a negative integer, zero, or a positive integer as the
	    *         first argument is less than, equal to, or greater than the
	    *         second.
	    */
	    @Override
	    public int compare(QREntryDist<?> o1, QREntryDist<?> o2) {
	        double d = o1.getDistance() - o2.getDistance();
	        return d < 0 ? -1 : (d > 0 ? 1 : 0);
	    }
	}

}