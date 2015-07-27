package controllers;

import java.io.File;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import models.MediaFile;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import views.html.index;
import views.html.show;

public class Application extends Controller {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	
	public static Result index() {
		List<MediaFile> mediaFiles = MediaFile.Finder.order("filename ASC").findList();
		return ok(index.render("Media DB", mediaFiles));
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

	private static void debugRequest(Request req) {
		StringBuilder sb = new StringBuilder("\n"+req.toString());
		sb.append("\n");
		for(String key : req.headers().keySet()) {
			sb.append("\t").append(key).append(" = ").append(req.getHeader(key)).append("\n");
		}
		sb.append("\n");
		Logger.debug(sb.toString());
	}
}
