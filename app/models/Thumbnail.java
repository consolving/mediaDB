package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;


@Entity
public class Thumbnail extends Model {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	public String filename;
	
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
			t.save();
		}
		return t;
	}
	
	public static List<Thumbnail> getLast(int number) {
		return Thumbnail.Finder.setMaxRows(number).findList();
	}
}
