package models;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.apache.commons.lang3.StringUtils;

import helpers.StopList;
import play.Logger;
import play.cache.Cache;
import play.db.ebean.Model;

@Entity
public class Tag extends Model implements Comparable<Tag> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;
	public String name;
	
    @ManyToMany(mappedBy = "tags", cascade=CascadeType.ALL )
    private Set<MediaFile> mediaFiles;
    
	public static Finder<Long, Tag> Finder = new Finder<Long, Tag>(Long.class, Tag.class);

	public static Set<Tag> findOrCreateTagsForText(String text) {
		Set<Tag> tags = new TreeSet<Tag>();
		for (String tag : getTagList(text)) {
			Logger.debug("tag: "+tag);
			if (tag.trim().length() > 1) {
				tags.add(Tag.findOrCreateTagByName(tag.trim()));
			}
		}
		return tags;
	}

	public static String tagsToIds(Set<Tag> tags) {
		Set<Long> ids = new HashSet<Long>();
		for (Tag tag : tags) {
			ids.add(tag.id);
		}
		return StringUtils.join(ids, ", ");
	}

	public static String tagsToNames(Set<Tag> tags) {
		Set<String> names = new HashSet<String>();
		for (Tag tag : tags) {
			names.add(tag.name);
		}
		return StringUtils.join(names, ", ");
	}

	public static Tag findOrCreateTagByName(String name) {
		String key = "tag_" + name;
		Tag tag = (Tag) Cache.get(key);
		if (tag == null) {
			tag = Tag.Finder.where().eq("name", name).findUnique();
		}
		if (tag == null) {
			tag = new Tag();
			tag.name = name;
			tag.save();
			Logger.debug("creating Tag: " + tag.toString());
		}
		Cache.set(key, tag);
		Logger.debug("using Tag: " + tag);
		return tag;
	}

	@Override
	public String toString() {
		return this.name;
	}

	private static Set<String> getTagList(String text) {
		text = cleanText(text);
		Set<String> tags = new HashSet<String>();
		for (String tag : text.split(" ")) {
			if (!StopList.LIST_DE.contains(tag.trim()) && !StopList.LIST_EN.contains(tag.trim())) {
				tags.add(tag);
			}
		}
		return tags;
	}

	private static String cleanText(String text) {
		text = text.toLowerCase();
		String[] findList = { "#", "%", ":", "=", "\n", "_", " -", "- ", "?", ". ", " .", ".", ",", " ,", ", ", "!", "/", "\\","\"", "„", "“", "(", ")", "[", "]", "{", "}", "<", ">" };
		for (String find : findList) {
			text = text.replace(find, " ");
		}
		return text.trim();
	}

	public int compareTo(Tag o) {
		if (o == null) {
			return 1;
		}
		return o.name.compareTo(this.name);
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Tag) {
			Tag t = (Tag) o;
			return t.name.equals(name);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
		return hash;
	}
}
