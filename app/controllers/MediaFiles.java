package controllers;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import fileauth.actions.BasicAuth;
import helpers.MediaFileHelper;
import models.MediaFile;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;
import views.html.MediaFiles.index;
import views.html.MediaFiles.show;

@BasicAuth
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
		
		ObjectNode out = (ObjectNode) Cache.get("folderStats");
		if(out == null) {
			out = Json.newObject();
			File rootFolder = new File(ROOT_DIR);
			ArrayNode dirSizes = out.arrayNode();
			ArrayNode dirCounts = out.arrayNode();
			if(rootFolder.exists()) {
				long sum = MediaFileHelper.getSize(rootFolder);
				long part = 0L;
				for(File folder : rootFolder.listFiles(FOLDER_FILTER)){
					part = MediaFileHelper.getSize(folder);
					ObjectNode dir = Json.newObject();
					dir.put("label", folder.getName()+" "+MediaFileHelper.humanReadableByteCount(part*1000, true));
					dir.put("value", 100*part/sum);
					dirSizes.add(dir);
					
					part = MediaFileHelper.getCount(folder);
					dir = Json.newObject();
					dir.put("label", folder.getName()+" "+part);			
					dir.put("value", part);
					dirCounts.add(dir);
				}
			}
			out.put("dirsSizes", dirSizes);
			out.put("dirsCounts", dirCounts);	
			Cache.set("folderStats", out, 60);
		}
		return ok(out);
	}
}
