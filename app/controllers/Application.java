package controllers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;
import java.util.TreeSet;

import com.typesafe.config.ConfigFactory;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

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
		String checksum = "";
		Set<File> mediaFiles = new TreeSet<File>();
		if (mediaFolder.exists()) {
			for (File f : mediaFolder.listFiles(FILE_NAME_FILTER)) {
				mediaFiles.add(f);
			}
		}
		return ok(index.render("Media DB", mediaFiles));
	}

	public static Result download(String filename) {
		File media = new File(ROOT_DIR + File.separator + "upload" + File.separator + filename);
		if (!media.exists()) {
			return notFound();
		}
		return ok(media);
	}

}
