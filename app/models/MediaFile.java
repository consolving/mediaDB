package models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class MediaFile extends Model {

	@Id
	public Long id;

	public String checksum;
	public String filename;
	public String path;
	
	@ManyToMany(cascade=CascadeType.ALL )
	private Set<Tag> tags;

	@OneToMany(mappedBy="mediaFile")
	private Set<Property> properties;
	
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
    
    public Map<String, Set> getTagsMap() {
        Map<String, Set> tagsMap = new HashMap<String, Set>();
        Set<String> tagsValue = new TreeSet<String>();
        for (Tag t : tags) {
            tagsValue.add(t.name);
        }
        tagsMap.put("tags", tagsValue);
        return tagsMap;
    }
}
