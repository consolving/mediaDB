package controllers.admin;

import java.util.List;

import controllers.Application;
import fileauth.actions.BasicAuth;
import models.MediaFile;
import play.mvc.Result;
import views.html.admin.Storage.index;

@BasicAuth
public class Storage extends Application {

	
	public static Result index() {
		Integer page = getQuereparameterAsInteger("page", 1);
		String sortedBy = getQuereparameterAsString("sortedBy", "filesize");
		String sortOrder = getQuereparameterAsString("sortOrder", "DESC");
		Integer mediaFilesCount = MediaFile.Finder.where().findRowCount();
		Integer max = (int) Math.ceil((double) mediaFilesCount / PER_PAGE); 
        Integer prev = page > 1 ? page - 1 : null;
        Integer next = page < max ? page + 1 : null;
		List<MediaFile> mediaFiles = MediaFile.getPageSortedBy(PER_PAGE, page-1, sortedBy, sortOrder);
		return ok(index.render(mediaFiles, prev, next, sortedBy, sortOrder));	
	}
}
