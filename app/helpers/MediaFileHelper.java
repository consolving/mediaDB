package helpers;

import java.io.File;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import models.Property;
import models.MediaFile;
import models.Tag;
import play.Logger;
import play.libs.Json;

public class MediaFileHelper {
	private MediaFileHelper() {
	}

	public static MediaFile probeFile(MediaFile mediaFile, File file) {
		Logger.debug("\n\nnew probe:" + file.getAbsolutePath());
		String json = FfmpegHelper.getFileProperties(file);
		JsonNode out = Json.parse(json);
		JsonNode format = out.get("format");
		if (format != null) {
			Property.getOrCreate(mediaFile, "format", format.toString());
			mediaFile = addKeyValues(mediaFile, format);
		}
		JsonNode streams = out.get("streams");
		if (streams != null) {
			Property.getOrCreate(mediaFile, "streams", streams.toString());
			Iterator<JsonNode> iter = streams.elements();
			while (iter.hasNext()) {
				mediaFile = addKeyValues(mediaFile, iter.next());
			}
		}
		return mediaFile;
	}

	public static MediaFile addTags(MediaFile mediaFile, String value) {
		if (value != null && !value.trim().isEmpty()) {
			mediaFile.setTags(Tag.findOrCreateTagsForText(value));
		}
		return mediaFile;
	}

	private static MediaFile addKeyValues(MediaFile mediaFile, JsonNode out) {
		if (out == null) {
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
