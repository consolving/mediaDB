package controllers;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import models.MediaFile;
import play.libs.Json;
import play.mvc.Result;
import views.html.MediaFiles.index;
import views.html.MediaFiles.show;

public class MediaFiles extends Application {
	
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static FileFilter FOLDER_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !pathname.getName().startsWith(".");
		}
	};	
	public static Result index() {
		List<MediaFile> mediaFiles = MediaFile.Finder.order("filename ASC").findList();
		return ok(index.render(mediaFiles));
	}

	public static Result show(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		if (mf != null) {
			return ok(show.render(mf));
		}
		return notFound();
	}

	public static Result download(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		File media = mf != null ? new File(ROOT_DIR + File.separator + "storage" + File.separator + mf.checksum) : null;
		if (media == null || !media.exists()) {
			return notFound();
		}
		debugRequest(request());
		response().setContentType(mf.mimeType);
		response().setHeader("Content-Disposition", "inline; filename=\"" + mf.filename + "\"");
		return ok(media);
	}
	
	public static Result folderStats() {
		File rootFolder = new File(ROOT_DIR);
		ObjectNode out = Json.newObject();
		ArrayNode dirs = out.arrayNode();
		if(rootFolder.exists()) {
			for(File folder : rootFolder.listFiles(FOLDER_FILTER)){
				ObjectNode dir = Json.newObject();
				dir.put("name", folder.getName());
				dir.put("size", FileUtils.sizeOfDirectory(folder));
				dir.put("count", folder.listFiles().length-1);
				dirs.add(dir);
			}
		}
		out.put("dirs", dirs);
		return ok(out);
	}
}
