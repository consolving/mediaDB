package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import play.Logger;
import play.db.ebean.Model;

@Entity
public class Property extends Model implements Comparable<Property> {

	@Id
	public Long id;	
	public String k;
	
	@Lob
	@Column(columnDefinition = "TEXT")		
	public String v;
	
	@ManyToOne
	public MediaFile mediaFile;
	
	public static Finder<Long, Property> Finder = new Finder<Long, Property>(Long.class, Property.class);

	public Property() {}
	
	public Property(String k, String v) {
		this.k = k.trim();
		this.v = v.trim();
	}
	
	@Override
	public String toString() {
		return this.k + " => " + this.v;
	}

	public static Property getOrCreate(MediaFile mediaFile, String k, String v) {
		Property p = Property.Finder.where().eq("mediaFile", mediaFile).eq("k", k).eq("v", v).findUnique();
		if(p == null) {
			p = new Property(k, v);
			p.mediaFile = mediaFile;
			p.save();
			Logger.debug("creating KeyValue: " + p.toString());
		} else {
			Logger.debug("using KeyValue: " + p.toString());
		}
		return p;
	}
	
	public int compareTo(Property o) {
		if (o == null) {
			return 1;
		}
		return o.v.compareTo(this.v)*o.k.compareTo(this.k);
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Property) {
			Property p = (Property) o;
			return p.v.equals(v) && p.k.equals(k);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + (this.v != null ? this.v.hashCode() : 0);
		return hash;
	}
}
