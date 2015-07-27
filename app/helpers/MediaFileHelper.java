package helpers;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;

import models.Property;
import models.MediaFile;
import models.Tag;
import play.Logger;
import play.libs.Json;

public class MediaFileHelper {
	private final static String FILE_BIN = ConfigFactory.load().getString("system.file.bin");
	private final static boolean HAS_FILE_BIN = new File(FILE_BIN).exists();
	
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
			String cmd = FILE_BIN + " --mime-type \""+ file.getAbsolutePath() + "\"";
			Logger.debug("running: "+cmd);
			String parts[] = SystemHelper.runCommand(cmd).split(":");
			return parts.length > 1 ? parts[parts.length-1].trim() : null;
		}
		return null;
	}
	public static MediaFile addTags(MediaFile mediaFile, String value) {
		if (value != null && !value.trim().isEmpty()) {
			mediaFile.setTags(Tag.findOrCreateTagsForText(value));
		}
		return mediaFile;
	}

	private static MediaFile addProperty(MediaFile mediaFile, String key, String value) {
		if(key != null && value != null && mediaFile != null) {
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
