package helpers;

import java.io.File;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;

import models.Property;
import models.MediaFile;
import models.Tag;
import play.Logger;
import play.libs.Json;

public class MediaFileHelper {
	
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
	
	public static String humanReadableCount(long value) {
	  if (value == Long.MIN_VALUE) return humanReadableCount(Long.MIN_VALUE + 1);
	  if (value < 0) return "-" + humanReadableCount(-value);
	  if (value < 1000) return Long.toString(value); //deal with easy case

	  Entry<Long, String> e = suffixes.floorEntry(value);
	  Long divideBy = e.getKey();
	  String suffix = e.getValue();

	  long truncated = value / (divideBy / 10); //the number part of the output times 10
	  boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
	  return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;		
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
}
