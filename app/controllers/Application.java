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
	protected final static Integer PER_PAGE = 256;
	
	public static Result index() {
		Integer page = getQuereparameterAsInteger("page", 1);	
		Integer mediaFileCount = MediaFile.Finder.findRowCount();
        Integer max = mediaFileCount / PER_PAGE;
        Integer prev = page > 1 ? page - 1 : null;
        Integer next = page < max ? page + 1 : null;		
        Integer tagCount = Tag.Finder.findRowCount();
		Integer propertyCount = Property.Finder.findRowCount();
		Integer thumbnailsCount = Thumbnail.Finder.findRowCount();
		return ok(index.render(mediaFileCount, thumbnailsCount, tagCount, propertyCount, PER_PAGE, page-1, prev, next));
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
	
	protected static Boolean getQuereparameterAsBoolean(String key, Boolean defaultValue) {
		Boolean value = request().getQueryString(key) != null ? Boolean.parseBoolean(request().getQueryString(key)) : null;
		return value != null ? value : defaultValue;
	}
	
	protected static Integer getQuereparameterAsInteger(String key, Integer defaultValue) {
		Integer value = null;
		try {
			value = request().getQueryString(key) != null ? Integer.parseInt(request().getQueryString(key)) : null;
		} catch(NumberFormatException ex) {
			Logger.warn(ex.getLocalizedMessage(), ex);
		}
		return value != null ? value : defaultValue;
	}
}
