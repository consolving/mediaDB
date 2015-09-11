package models;

import java.io.File;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import helpers.ThumbnailsHelper;
import play.db.ebean.Model;


@Entity
public class Thumbnail extends Model {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	
	public String filepath;
	public String checksum;
	
	@ManyToOne
	public MediaFile mediaFile;
	
	public Thumbnail(String path) {
		this.filepath = path.trim();
	}
	
	public static Finder<Long, Thumbnail> Finder = new Finder<Long, Thumbnail>(Long.class, Thumbnail.class);

	public String getChecksum(File file) {
		if(checksum == null) {
			checksum = ThumbnailsHelper.getETag(file);
			update();
		}
		return checksum;
	}
	
	public static Thumbnail getOrCreate(MediaFile mediaFile, String filepath, String checksum) {
		Thumbnail t = Thumbnail.Finder.where().eq("mediaFile", mediaFile).eq("filepath", filepath.trim()).findUnique();
		if(t == null) {
			t = new Thumbnail(filepath);
			t.mediaFile = mediaFile;
			t.checksum = checksum;
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
