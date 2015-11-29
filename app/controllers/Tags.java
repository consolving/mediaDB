package controllers;

import java.util.List;
import java.util.Set;

import fileauth.actions.BasicAuth;
import models.MediaFile;
import models.Tag;
import play.mvc.Result;
import views.html.Tags.show;

@BasicAuth
public class Tags extends Application {

	public static Result index() {
		return ok("");
	}

	public static Result show(String queryTags) {
		if (queryTags == null) {
			return redirect(routes.Application.index());
		}
		Integer page = getQuereparameterAsInteger("page", 1);
		Set<Tag> tags = Tag.findOrCreateTagsForText(queryTags);
		Integer mediaFilesCount = MediaFile.getCountForTags(tags);
		Integer max = (int) Math.ceil((double)mediaFilesCount  / PER_PAGE);
		Integer prev = page > 1 ? page - 1 : null;
		Integer next = page < max ? page + 1 : null;
		List<MediaFile> mediaFiles = MediaFile.getForTags(PER_PAGE, page - 1, tags);
		return ok(show.render(queryTags, mediaFiles, prev, next));
	}

}
