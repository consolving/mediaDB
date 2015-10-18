package controllers;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;

import fileauth.actions.BasicAuth;
import helpers.MediaFileHelper;
import helpers.ThumbnailsHelper;
import models.MediaFile;
import models.Thumbnail;
import play.Logger;
import play.cache.Cache;
import play.mvc.Result;
import views.html.MediaFiles.index;
import views.html.MediaFiles.message;
import views.html.MediaFiles.show;
import views.html.MediaFiles.properties;
import views.html.MediaFiles.thumbnails;

@BasicAuth
public class MediaFiles extends Application {

	public static Result index(String type) {
		Integer page = getQuereparameterAsInteger("page", 1);
		Integer mediaFilesCount = MediaFile.Finder.where().startsWith("mimeType", type).findRowCount();
        Integer max = mediaFilesCount / PER_PAGE;
        Integer prev = page > 1 ? page - 1 : null;
        Integer next = page < max ? page + 1 : null;		
		List<MediaFile> mediaFiles = MediaFile.getMimeType(PER_PAGE, page-1, type);
		return ok(index.render(mediaFiles, mediaFilesCount, type, prev, next));		
	}
	
	public static Result show(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		if (mf != null) {
			return ok(show.render(mf));
		}
		return notFound();
	}

	public static Result properties(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		if (mf != null) {
			return ok(properties.render(mf));
		}		
		return notFound();		
	}

	public static Result thumbnails(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		if (mf != null) {
			return ok(thumbnails.render(mf));
		}		
		return notFound();		
	}
	
	public static Result delete(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		if(mf == null) {
			return redirect(routes.Application.index());
		}
		String type = mf.mimeType;
		mf.deleteManyToManyAssociations("tags");
		for(models.Property prop : mf.getProperties()) {
			prop.delete();
		}		
		ThumbnailsHelper.deleteThumbnails(mf);
		MediaFileHelper.deleteFile(mf);
		mf.delete();
		return ok(message.render(mf.filename+ " was deleted.", routes.MediaFiles.index(type).url()));
	}
	
	public static Result cover(String checksum, Integer index) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		Thumbnail thumb = mf != null && mf.getThumbnail() != null ? mf.getThumbnail() : null;
		File media = thumb != null ? new File(THUMBNAILS_DIR + File.separator + thumb.filepath) : null;		
		
		if(media != null && !media.exists() && thumb.checksum != null) {
			File oldThumb = new File(THUMBNAILS_DIR + File.separator + mf.checksum + "thumb_0_800x600.png");
			ThumbnailsHelper.checkThumbnailLocation(mf, media, oldThumb);
		}
		
		if(media == null || thumb.checksum == null) {
			media = ThumbnailsHelper.createThumbnail(mf, "800x600");
		}

		if (thumb == null) {
			Logger.warn("thumb for " + checksum + " is null!");
			return notFound();
		}
		
		if (media == null || !media.exists()) {
			Logger.warn("cannot find " + media.getAbsolutePath());
			return notFound();
		}
		
		response().setContentType("image/png");
		response().setHeader(CACHE_CONTROL, "max-age=3600");
		response().setHeader(ETAG, thumb.getChecksum(media));		
		response().setHeader("Content-Disposition", "inline; filename=\"" + media.getName() + "\"");		
		return ok(media);
	}
	
	public static Result thumbnail(Long id) {
		Thumbnail thumb = Thumbnail.Finder.byId(id);
		File media = thumb != null ? new File(THUMBNAILS_DIR + File.separator + thumb.filepath) : null;		
		if(thumb == null || media == null || !media.exists()) {
			return notFound();
		}
		response().setContentType("image/png");
		response().setHeader(CACHE_CONTROL, "max-age=3600");
		response().setHeader(ETAG, thumb.getChecksum(media));		
		response().setHeader("Content-Disposition", "inline; filename=\"" + media.getName() + "\"");		
		return ok(media);		
	}
	
	public static Result download(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		File media = mf != null ? new File(STORAGE_FOLDER + File.separator + mf.getLocation()) : null;
		Logger.debug("download " + (media != null ? media.getAbsolutePath() : null));
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
			Logger.warn("still calculation first Folder Stats");
			// TODO add Future for Response
			return notFound();
		}
		return ok(out);
	}
	
}
