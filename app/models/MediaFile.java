package models;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;

import play.db.ebean.Model;

@Entity
public class MediaFile extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	public String checksum;
	public String filename;
	
	public Long filesize;
	public String mimeType;
	
	public Date lastCheck;
	public Date created;
	
	@ManyToMany(cascade=CascadeType.ALL )
	private Set<Tag> tags;

	@OneToMany(mappedBy="mediaFile")
	@OrderBy("k DESC")
	private List<Property> properties;

	@OneToMany(mappedBy="mediaFile")
	@OrderBy("filename DESC")
	private List<Thumbnail> thumbnails;
	
	public static Finder<Long, MediaFile> Finder = new Finder<Long, MediaFile>(Long.class, MediaFile.class);

    public void setTags(Set<Tag> tags) {
        if (this.tags == null) {
            this.tags = new TreeSet<Tag>();
        }
        for (Tag t : tags) {
            this.tags.add(t);
        }
    }

    public List<Tag> getTags() {
    	return Tag.Finder.where().eq("mediaFiles.id", this.id).orderBy("name ASC").findList();
    }
    
    public List<Property> getProperties() {
    	return Property.Finder.where().eq("mediaFile.id", this.id).orderBy("k ASC").findList();
    } 
    
    public String getThumbnail() {
    	return thumbnails.size() > 0 ? thumbnails.get(0).filename : null;
    }
    
    public Map<String, Set<String>> getTagsMap() {
        Map<String, Set<String>> tagsMap = new HashMap<String, Set<String>>();
        Set<String> tagsValue = new TreeSet<String>();
        for (Tag t : tags) {
            tagsValue.add(t.name);
        }
        tagsMap.put("tags", tagsValue);
        return tagsMap;
    }
    
    public void checked() {
    	SqlUpdate update = Ebean. createSqlUpdate("UPDATE media_file SET last_check=:lastCheck, filesize=:filesize, created=:created WHERE checksum=:checksum")
		.setParameter("lastCheck", new Date())
		.setParameter("created", this.created)
		.setParameter("filesize", this.filesize)
		.setParameter("checksum", this.checksum);
		update.execute();
    }
    
    public String toString() {
    	return filename+ "("+checksum+")";
    }
}
