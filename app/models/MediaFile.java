package models;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

@Entity
public class MediaFile extends Model {
	
	@Id
	public Long id;
	
	public String checksum;
	public String filename;
	
	public static Finder<Long,MediaFile> Finder = new Finder<Long,MediaFile>(Long.class, MediaFile.class);

}
