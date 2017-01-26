package models;

import java.io.IOException;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import helpers.MediaFileHelper;
import play.Logger;
import play.db.ebean.Model;

@Entity
public class MediaFile extends Model {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;

	public String filepath;
	public String checksum;
	public String filename;
	
	public Long filesize;
	public String mimeType;
	
	public Date lastCheck;
	public Date created;
	
	@OneToOne
	public Thumbnail cover;
	
	@ManyToOne
	public MediaFolder folder = null;
	
	@ManyToMany(cascade=CascadeType.ALL )
	private Set<Tag> tags;

	@OneToMany(mappedBy="mediaFile")
	@OrderBy("k DESC")
	private List<Property> properties;

	@OneToMany(mappedBy="mediaFile")
	@OrderBy("filename DESC")
	private List<Thumbnail> thumbnails;
	
	@Transient
	private Map<String, String> props = null;
	
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
 
    public List<Property> getProperties(String k) {
    	return Property.Finder.where().eq("mediaFile.id", this.id).eq("k", k.trim()).findList();
    } 
       
    public Property getProperty(String k) {
    	List<Property> props = getProperties(k);
    	return props.size() > 0 ? props.get(0) : null;
    }
    
    public Thumbnail getThumbnail() {
    	return cover != null ? cover : thumbnails.size() > 0 ? thumbnails.get(0) : null;
    }
    
    public List<Thumbnail> getThumbnails() {
    	return thumbnails;
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
    
    public Map<String, String> getMDBProperties() {
    	if(props == null) {
	    	props = new HashMap<String, String>();
	    	for(Property p : Property.Finder.where().eq("mediaFile", this).startsWith("k", "mediaDB").findList()) {
	    		props.put(p.k.replace("mediaDB/", ""), p.v);
	    	}
    	}
    	return props;
    }
    
    public void checked() {
    	SqlUpdate update = Ebean.createSqlUpdate("UPDATE media_file SET last_check=:lastCheck, filesize=:filesize, created=:created WHERE checksum=:checksum")
		.setParameter("lastCheck", new Date())
		.setParameter("created", this.created)
		.setParameter("filesize", this.filesize)
		.setParameter("checksum", this.checksum);
		update.execute();
    }
    
    public String toString() {
    	return filename+ "("+checksum+")";
    }
	
    public String getLocation() {
    	if(this.filepath == null) {
    		this.filepath = MediaFileHelper.checkMediaFileLocation(this);
    		update();
    	}
    	return this.filepath;
    }
    
	public JsonNode getStreams() {
		Property prop = Property.Finder.where().eq("mediaFile", this).eq("k", "streams").findUnique();
		JsonNode arrNode = null;
		try {
			if(prop != null) {
				arrNode = new ObjectMapper().readTree(prop.v);
				if(arrNode.isArray())
				for (final JsonNode objNode : arrNode) {
					Logger.debug("arrNode: "+objNode);  
				}
			}
			return arrNode != null && arrNode.isArray() ? arrNode : new ObjectMapper().readTree("[]");
		} catch (IOException ex) {
			Logger.warn(ex.getLocalizedMessage(), ex);
			return null;
		}			
	}
	
    public static Integer getSize() {
		return Finder.findRowCount();
	}
	
    public static List<MediaFile> nextChecks(int number) {
    	return Finder.setMaxRows(number).where().isNull("lastCheck").order("created ASC").findList();
    }
	public static List<MediaFile> getLast(int number, int page) {
		return Finder.orderBy("id DESC").findPagingList(number).getPage(page).getList();
	}    
	
	public static List<MediaFile> getPageSortedBy(int number, int page, String sortedBy, String sortOrder) {
		return Finder.orderBy(sortedBy+" "+sortOrder.toUpperCase()).findPagingList(number).getPage(page).getList(); 
	}
	
	public static List<MediaFile> getMimeType(int number, int page, String mimeType) {
		return Finder.orderBy("id DESC").where().startsWith("mimeType", mimeType.trim()).findPagingList(number).getPage(page).getList();
	}

    public static List<MediaFile> getForTags(int number, int page, Set<Tag> tags) {
    	return Finder.where().in("tags", tags).orderBy("id DESC").findPagingList(number).getPage(page).getList();
    }
    
    public static Integer getCountForTags(Set<Tag> tags) {
    	return Finder.where().in("tags", tags).findRowCount();
    }  
    
	public static Map<String, Long> getFileSizes() {
		Map<String, Long> sizes = new HashMap<>();
		SqlQuery query = Ebean.createSqlQuery(
				"SELECT sum(filesize) as size, mime_type FROM media_file group by mime_type order by size DESC");
		List<SqlRow> rows = query.findList();
		for (SqlRow sqlRow : rows) {
			if(sqlRow.getString("mime_type") != null && sqlRow.getLong("size") != null) {
				sizes.put(sqlRow.getString("mime_type"), sqlRow.getLong("size"));				
			}
		}
		return sizes;
	}
}
