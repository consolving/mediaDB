package controllers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import models.MediaFile;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;
import views.html.show;

public class Application extends Controller {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static FilenameFilter FILE_NAME_FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File arg0, String name) {
			return !name.startsWith(".");
		}

	};

	public static Result index() {
		File mediaFolder = new File(ROOT_DIR + File.separator + "upload");
		List<MediaFile> mediaFiles = MediaFile.Finder.all();
		return ok(index.render("Media DB", mediaFiles));
	}

	public static Result show(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		if(mf != null) {
			return ok(show.render(mf));
		}
		return notFound();
	}
	
	public static Result download(String checksum) {
		MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
		File media = mf != null ? new File(ROOT_DIR + File.separator + "upload" + File.separator + mf.filename) : null;
		if (media == null || !media.exists()) {
			return notFound();
		}
		return ok(media);
	}

}
