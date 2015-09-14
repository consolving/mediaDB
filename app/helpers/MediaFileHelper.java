package helpers;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import models.MediaFile;
import models.Property;
import models.Tag;
import models.Thumbnail;
import play.Logger;
import play.libs.Json;

public class MediaFileHelper {
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String STORAGE_FOLDER = ROOT_DIR + File.separator + "storage";
	private final static FileFilter FOLDER_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !pathname.getName().startsWith(".");
		}
	};
	private static final NavigableMap<Long, String> suffixes = new TreeMap<> ();
	static {
	  suffixes.put(1_000L, "k");
	  suffixes.put(1_000_000L, "M");
	  suffixes.put(1_000_000_000L, "G");
	  suffixes.put(1_000_000_000_000L, "T");
	  suffixes.put(1_000_000_000_000_000L, "P");
	  suffixes.put(1_000_000_000_000_000_000L, "E");
	}
	
	private final static String FILE_BIN = ConfigFactory.load().getString("system.file.bin");
	private final static boolean HAS_FILE_BIN = new File(FILE_BIN).exists();
	private final static String DU_BIN = ConfigFactory.load().getString("system.du.bin");
	private final static boolean HAS_DU_BIN = new File(DU_BIN).exists();
	private final static String MV_BIN = ConfigFactory.load().getString("system.mv.bin");
	private final static boolean HAS_MV_BIN = new File(MV_BIN).exists();
	private final static String LS_BIN = ConfigFactory.load().getString("system.ls.bin");
	private final static boolean HAS_LS_BIN = new File(LS_BIN).exists();
	private final static String WC_BIN = ConfigFactory.load().getString("system.wc.bin");
	private final static boolean HAS_WC_BIN = new File(WC_BIN).exists();
	private final static String RM_BIN = ConfigFactory.load().getString("system.rm.bin");
	private final static boolean HAS_RM_BIN = new File(RM_BIN).exists();
	
	private MediaFileHelper() {
	}

	public static MediaFile probeFile(MediaFile mediaFile, File file) {
		Logger.debug("\n\nnew probe:" + file.getAbsolutePath());
		String json = FfmpegHelper.getFileProperties(file);
		JsonNode out = Json.parse(json);
		JsonNode format = out.get("format");
		if (format != null) {
			Property.getOrCreate(mediaFile, "format", format.toString());
			mediaFile = addProperties(mediaFile, format);
		}
		JsonNode streams = out.get("streams");
		if (streams != null) {
			Property.getOrCreate(mediaFile, "streams", streams.toString());
			Iterator<JsonNode> iter = streams.elements();
			while (iter.hasNext()) {
				mediaFile = addProperties(mediaFile, iter.next());
			}
		}
		return mediaFile;
	}

	public static String getFileMimeType(File file) {
		if (HAS_FILE_BIN && file.exists()) {
			String cmd = FILE_BIN + " --mime-type \"" + file.getAbsolutePath() + "\"";
			Logger.debug("running: " + cmd);
			String parts[] = SystemHelper.runCommand(cmd).split(":");
			return parts.length > 1 ? parts[parts.length - 1].trim() : null;
		} else {
			Logger.warn(FILE_BIN + " not found!");
		}
		return null;
	}

	public static Long getSize(File file) {
		if (HAS_DU_BIN && file.exists()) {
			String cmd;
			if(file.isFile() && file.getAbsolutePath().endsWith("/")) {
				cmd = DU_BIN + " -s \"" + file.getAbsolutePath().substring(0, file.getAbsolutePath().length()-1) + "\"";	
			} else {
				cmd = DU_BIN + " -s \"" + file.getAbsolutePath() + "\"";
			}
			Logger.debug("running: " + cmd);
			String parts[] = SystemHelper.runCommand(cmd).split("\t");
			try {
				return !parts[0].isEmpty() ? Long.parseLong(parts[0]) : null;
			} catch (NumberFormatException ex) {
				Logger.error(ex.getLocalizedMessage(), ex);
			}
		} else {
			Logger.warn(DU_BIN + " not found!");
		}
		return null;
	}

	public static Long getCount(File file) {
		if (HAS_WC_BIN && HAS_LS_BIN && file.exists() && !file.isFile()) {
			String cmd = LS_BIN+" -l \"" + file.getAbsolutePath() + "/\" | "+WC_BIN+" -l";
			Logger.debug("running: " + cmd);
			String part = SystemHelper.runCommand(cmd).trim();
			try {
				return !part.equals("0") ? Long.parseLong(part)-1 : Long.parseLong(part);
			} catch (NumberFormatException ex) {
				Logger.error(ex.getLocalizedMessage(), ex);
			}			
		}
		return null;
	}
	
	public static boolean delete(File file) {
		if (HAS_RM_BIN && file.exists()) {
			String cmd = RM_BIN+" -Rf \"" + file.getAbsolutePath() + "/\"";
			Logger.debug("running: " + cmd);
			String part = SystemHelper.runCommand(cmd).trim();
			Logger.debug(part);
			return !file.exists();
		}
		return false;
	}
	
	public static boolean delete(String filename) {
		File file = new File(filename);
		if (HAS_RM_BIN && file.exists()) {
			String cmd = RM_BIN+" -Rf \"" + file.getAbsolutePath() + "/\"";
			Logger.debug("running: " + cmd);
			String part = SystemHelper.runCommand(cmd).trim();
			Logger.debug(part);
			return !file.exists();
		}
		return false;
	}

	public static String humanReadableCount(Long value) {
		if (value == null) 
			return humanReadableCount(0L);
		if (value == Long.MIN_VALUE)
			return humanReadableCount(Long.MIN_VALUE + 1);
		if (value < 0)
			return "-" + humanReadableCount(-value);
		if (value < 1000)
			return Long.toString(value); // deal with easy case

		Entry<Long, String> e = suffixes.floorEntry(value);
		Long divideBy = e.getKey();
		String suffix = e.getValue();

		long truncated = value / (divideBy / 10); // the number part of the
													// output times 10
		boolean hasDecimal = truncated < 100
				&& (truncated / 10d) != (truncated / 10);
		return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10)
				+ suffix;
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
		int unit = si ? 1000 : 1024;
		if (bytes < unit)
			return bytes + " B";
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}

	public static MediaFile addTags(MediaFile mediaFile, String value) {
		if (value != null && !value.trim().isEmpty()) {
			mediaFile.setTags(Tag.findOrCreateTagsForText(value));
		}
		return mediaFile;
	}
	
	public static Date fileTimeToDate(FileTime fileTime) {
		long milliseconds = fileTime.to(TimeUnit.MILLISECONDS);
        if((milliseconds > Long.MIN_VALUE) && (milliseconds < Long.MAX_VALUE)) {
            return new Date(fileTime.to(TimeUnit.MILLISECONDS));
        }
        return null;
	}
	
	public static ObjectNode addCounts(ObjectNode out) {
		ArrayNode counts = out.arrayNode();
		ObjectNode dir;
		dir= Json.newObject();
		dir.put("label", "Media Files\n"+MediaFile.getSize());
		dir.put("value",  MediaFile.getSize());
		counts.add(dir);
		dir = Json.newObject();
		dir.put("label", "Thumbnails\n"+Thumbnail.getSize());
		dir.put("value",  Thumbnail.getSize());
		counts.add(dir);
		out.put("dirsCounts", counts);
		return out;
	}
	
	public static ObjectNode addFolderSizes(ObjectNode out) {
		File rootFolder = new File(ROOT_DIR);
		ArrayNode dirSizes = out.arrayNode();
		if (rootFolder.exists()) {
			Long sum = MediaFileHelper.getSize(rootFolder);
			Long size = 0L;
			ObjectNode dir;
			for (File folder : rootFolder.listFiles(FOLDER_FILTER)) {
				size = MediaFileHelper.getSize(folder);
				if (size != null && sum != null) {
					if(MediaFileHelper.getCount(folder) > 0) {					
						dir = Json.newObject();
						dir.put("label", folder.getName() + "\n" + MediaFileHelper.humanReadableByteCount(size * 1000, true));
						dir.put("value", (sum==0 ? 0 : 100 * size / sum));
						dirSizes.add(dir);
					}
				}
			}
		}
		out.put("dirsSizes", dirSizes);
		return out;
	}
	
	public static ObjectNode addTypeCounts(ObjectNode out) {
		String sql = "SELECT DISTINCT mime_type, count(*) as mime_type_count FROM media_file group by mime_type ORDER BY mime_type_count DESC;";
		ArrayNode types = out.arrayNode();
		ObjectNode type;
		List<SqlRow> sqlRows = Ebean.createSqlQuery(sql).findList();
		for(SqlRow row : sqlRows) {
			type = Json.newObject();
			type.put("label", row.getString("mime_type"));
			type.put("value", row.getInteger("mime_type_count"));
			types.add(type);
		}
		out.put("typeCounts", types);
		return out;
	}
	
	public static String shortName(String name, int length) {
		return name != null ? name.length() > length ? name.substring(0, length) + "…" : name : name;
	}
	
	public static void addDuration(MediaFile mediaFile) {
		if (mediaFile.mimeType.startsWith("video")) {
			Long duration = aggregateDuration(mediaFile);
			Logger.debug("raw duration is: "+duration);
			addProperty(mediaFile, "mediaDB/duration", String.valueOf(duration));
			addProperty(mediaFile, "mediaDB/durationString", getDurationString(duration));
		}
	}
	
	public static void addDimensions(MediaFile mediaFile) {
		if (mediaFile.mimeType.startsWith("video") || mediaFile.mimeType.startsWith("image")) {
			Property p = mediaFile.getProperty("width");
			if(p != null) {
				addProperty(mediaFile, "mediaDB/width", p.v);				
			}
			p = mediaFile.getProperty("height");
			if(p != null) {
				addProperty(mediaFile, "mediaDB/height", p.v);			
			}
		}
	}
	
	private static String getDurationString(Long duration) {
		int seconds = (int)(duration%60);
		int rest2 = (int)((duration - seconds)/60);
		int minutes = (int)(rest2%60);
		int hours = (int)(rest2/60);	
		Logger.debug("got: " + duration + " => " + String.format("%02d:%02d:%02d", hours, minutes, seconds));
		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
	}
	
	private static long aggregateDuration(MediaFile mediaFile) {
		Long duration = 0L;
		int count = 0;
		float durationFloat;
		for(Property p : mediaFile.getProperties("duration")) {
			try {
				durationFloat = Float.parseFloat(p.v);
				duration += (long)Math.ceil(durationFloat);
				count++;
			} catch(NumberFormatException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		}
		return duration/count;
	}
	
	public static void deleteFile(MediaFile mediaFile) {
		File file = new File(STORAGE_FOLDER+File.separator+SystemHelper.getFolders(mediaFile.checksum));
		if(file.exists()) {
			FileUtils.deleteQuietly(file);	
		}		
	}

	public static String checkMediaFileLocation(MediaFile mediaFile) {
		String newFolder = SystemHelper.getFolders(mediaFile.checksum);
		File file = new File(STORAGE_FOLDER + File.separator + newFolder + File.separator + mediaFile.checksum);
		File oldFile = new File(STORAGE_FOLDER + File.separator + mediaFile.checksum);
		if (oldFile.exists()) {
			try {
				Logger.info("moving " + oldFile.getAbsolutePath() + " to " + file.getAbsolutePath());
				checkDir(newFolder);
				mediaFile.filepath = SystemHelper.getFoldersForName(mediaFile.checksum);
				FileUtils.moveFile(oldFile, file);
				mediaFile.update();
				Logger.info("deleting " + oldFile.getAbsolutePath());
				FileUtils.deleteQuietly(oldFile);
			} catch (IOException ex) {
				Logger.warn(ex.getLocalizedMessage(), ex);
			}
		}
		return mediaFile.filepath;
	}
	
	private static MediaFile addProperty(MediaFile mediaFile, String key, String value) {
		if (key != null && value != null && mediaFile != null) {
			Property.getOrCreate(mediaFile, key, value);
		}
		return mediaFile;
	}

	private static MediaFile addProperties(MediaFile mediaFile, JsonNode out) {
		if (out == null || mediaFile == null) {
			return mediaFile;
		}
		Iterator<Entry<String, JsonNode>> ite = out.fields();
		while (ite.hasNext()) {
			Entry<String, JsonNode> temp = ite.next();
			Property.getOrCreate(mediaFile, temp.getKey(), temp.getValue().asText());
			mediaFile = addTags(mediaFile, temp.getValue().asText());
		}
		return mediaFile;
	}
	
	private static void checkDir(String path) {
		if (!new File(path).exists()) {
			new File(path).mkdirs();
		}
	}	
}
