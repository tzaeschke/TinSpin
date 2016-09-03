/*
 * Copyright 2011-2016 ETH Zurich. All Rights Reserved.
 *
 * This software is the proprietary information of ETH Zurich.
 * Use is subject to license terms.
 */
package ch.ethz.globis.tinspin.data.osm;


class OSMEntry {
	private long id; 
	private double lat; 
	private double lon; 
	private int uid; 
	private boolean visible; 
	private int version;
	private int changeset;
	private long date;
	
	@SuppressWarnings("unused")
	private OSMEntry() {
		// For ZooDB
	}
	
	public OSMEntry(long id, double lat, double lon, int uid, boolean visible, int version, 
			int changeset, long date) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.uid = uid;
		this.visible = visible;
		this.version = version;
		this.changeset = changeset;
		this.date = date;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(100);
		sb.append("(id=");
		sb.append(id);
		sb.append(", lat=");
		sb.append(lat);
		sb.append(", lon=");
		sb.append(lon);
		sb.append(", uid=");
		sb.append(uid);
		sb.append(", vis=");
		sb.append(visible);
		sb.append(", ver=");
		sb.append(version);
		sb.append(", chg=");
		sb.append(changeset);
		sb.append(", date=");
		sb.append(date);
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof OSMEntry)) {
			return false;
		}
		OSMEntry o = (OSMEntry) obj;
		return id==o.id && lat==o.lat && lon==o.lon && uid==o.uid && version==o.version && 
				visible==o.visible && changeset==o.changeset && date==o.date;
	}
	
	@Override
	public int hashCode() {
		return (int) (Double.doubleToLongBits(lon) & Double.doubleToLongBits(lat) & date & id & uid);
	}
}