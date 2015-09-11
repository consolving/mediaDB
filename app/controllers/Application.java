package controllers;

import java.io.File;

import com.typesafe.config.ConfigFactory;

import fileauth.actions.BasicAuth;
import models.MediaFile;
import models.Property;
import models.Tag;
import models.Thumbnail;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.Result;
import views.html.index;

@BasicAuth
public class Application extends Controller {

	protected final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	protected final static String THUMBNAILS_DIR = ROOT_DIR + File.separator + "thumbnails";
	protected final static String STORAGE_FOLDER = ROOT_DIR + File.separator + "storage";
	
	public static Result index() {
		int mediaFileCount = MediaFile.Finder.findRowCount();
		int tagCount = Tag.Finder.findRowCount();
		int propertyCount = Property.Finder.findRowCount();
		int thumbnailsCount = Thumbnail.Finder.findRowCount();
		return ok(index.render(mediaFileCount, thumbnailsCount, tagCount, propertyCount));
	}
	
	protected static void debugRequest(Request req) {
		StringBuilder sb = new StringBuilder("\n"+req.toString());
		sb.append("\n");
		for(String key : req.headers().keySet()) {	
			sb.append("\t").append(key).append(" = ");
			if(key.equals("Authorization")) {
				sb.append("*************").append("\n");				
			} else {
				sb.append(req.getHeader(key)).append("\n");
			}
		}
		sb.append("\n");
		Logger.debug(sb.toString());
	}
}
