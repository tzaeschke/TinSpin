package org.zoodb.index.quadtree;

import java.util.Comparator;

public class QEntryDist<T> extends QEntry<T> {
	private double distance;
	
	public QEntryDist(QEntry<T> e, double dist) {
		super(e.getPoint(), e.getValue());
		this.distance = dist;
	}
	
	public double getDistance() {
		return distance;
	}
	
	public static final QEntryComparator COMP = new QEntryComparator();
	
	static class QEntryComparator implements Comparator<QEntryDist<?>> {

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
	    public int compare(QEntryDist<?> o1, QEntryDist<?> o2) {
	        double d = o1.getDistance() - o2.getDistance();
	        return d < 0 ? -1 : (d > 0 ? 1 : 0);
	    }
	}

}