package models;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import play.Logger;
import play.cache.Cache;
import play.db.ebean.Model;

@Entity
public class Configuration extends Model {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long id;	
	public String k;
	
	@Lob
	@Column(columnDefinition = "TEXT")		
	public String v;

	public String hostname;
	
	public static Finder<Long, Configuration> Finder = new Finder<Long, Configuration>(Long.class, Configuration.class);
	
	public static void set(String key, String value) {
		set(getLocalHostname(), key, value);
	}
	
	public static void set(String hostname, String key, String value) {
		Configuration c = Configuration.Finder.where().eq("hostname", hostname).eq("k", key.trim()).findUnique();
		String v = value != null ? value.trim() : null;
		if(c != null) {
			c.v = v;
		} else {
			c = new Configuration();
			c.hostname = hostname.trim();
			c.k = key.trim();
			c.v = v;
		}
		c.save();
		Cache.set(key, v);	
	}
	
	public static String get(String key) {
		return get(getLocalHostname(), key);
	}
	
	public static String get(String hostname, String key) {
		String value = (String) Cache.get(key);
		if(value == null) {
			Configuration c = Configuration.Finder.where().eq("hostname", hostname).eq("k", key.trim()).findUnique();
			value =  c != null ? c.v : null;
		}
		Cache.set(key, value);	
		return value;
	}
	
	private static String getLocalHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException ex) {
			Logger.error(ex.getLocalizedMessage(), ex);
			return "localhost";
		}
	}
}
