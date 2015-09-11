package models;

import java.io.File;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import helpers.ThumbnailsHelper;
import play.Logger;
import play.db.ebean.Model;


@Entity
public class Thumbnail extends Model {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	public String filename;
	public String checksum;
	
	@ManyToOne
	public MediaFile mediaFile;
	
	public Thumbnail(String filename) {
		this.filename = filename.trim();
	}
	
	public static Finder<Long, Thumbnail> Finder = new Finder<Long, Thumbnail>(Long.class, Thumbnail.class);

	public static Thumbnail getOrCreate(MediaFile mediaFile, String filename) {
		Thumbnail t = Thumbnail.Finder.where().eq("mediaFile", mediaFile).eq("filename", filename.trim()).findUnique();
		if(t == null) {
			t = new Thumbnail(filename);
			t.mediaFile = mediaFile;
			t.checksum = ThumbnailsHelper.getETag(new File(filename));
			t.save();
		}
		return t;
	}
	
	public static Integer getSize() {
		return Finder.findRowCount();
	}
	
	public static List<Thumbnail> getLast(int number) {
		return Finder.setMaxRows(number).orderBy("id DESC").findList();
	}
}
