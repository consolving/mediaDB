package controllers;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import fileauth.actions.BasicAuth;
import models.MediaFile;
import play.cache.Cache;
import play.libs.Json;
import play.mvc.Result;
import views.html.MediaFiles.index;
import views.html.MediaFiles.show;

@BasicAuth
public class MediaFiles extends Application {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");

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
		if (out == null) {
			out = Json.newObject();
		}
		return ok(out);
	}
}
