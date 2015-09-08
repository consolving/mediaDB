package controllers;

import helpers.MediaFileHelper;
import helpers.ThumbnailsHelper;

import java.io.File;
import java.util.List;

import models.MediaFile;
import play.cache.Cache;
import play.mvc.Result;
import views.html.MediaFiles.index;
import views.html.MediaFiles.show;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import fileauth.actions.BasicAuth;

@BasicAuth
public class MediaFiles extends Application {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");


	public static Result index(String type) {
		List<MediaFile> mediaFiles = MediaFile.getMimeType(512, 0, type);
		Integer mediaFilesCount = MediaFile.Finder.findRowCount();
		return ok(index.render(mediaFiles, mediaFilesCount));		
	}
	
	public static Result show(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		if (mf != null) {
			return ok(show.render(mf));
		}
		return notFound();
	}

	public static Result thumbnail(String checksum, Integer index) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		File media = mf != null && mf.getThumbnail() != null ? new File(mf.getThumbnail()) : null;
		if(media == null) {
			media = ThumbnailsHelper.createThumbnail(mf, "800x600");
		}
		if (media == null || !media.exists()) {
			return notFound();
		}
		response().setContentType("image/png");
		response().setHeader(CACHE_CONTROL, "max-age=3600");
		response().setHeader(ETAG, ThumbnailsHelper.getETag(media));		
		response().setHeader("Content-Disposition", "inline; filename=\"" + media.getName() + "\"");		
		return ok(media);
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
			out = MediaFileHelper.getFolderSizes();
			Cache.set("folderStats", out, 3600);
		}
		return ok(out);
	}
}
