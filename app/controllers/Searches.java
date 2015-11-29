package controllers;

import java.util.List;
import java.util.Set;

import models.MediaFile;
import models.Tag;
import play.mvc.Result;
import views.html.Searches.result;

public class Searches extends Application {
	public static Result result() {
		String query = getQuereparameterAsString("query", "");
		Integer page = getQuereparameterAsInteger("page", 1);
		Set<Tag> tags = Tag.findOrCreateTagsForText(query);
		Integer mediaFilesCount = MediaFile.getCountForTags(tags);
		Integer max = (int) Math.ceil((double) mediaFilesCount / PER_PAGE);
		Integer prev = page > 1 ? page - 1 : null;
		Integer next = page < max ? page + 1 : null;
		List<MediaFile> mediaFiles = MediaFile.getForTags(PER_PAGE, page - 1, tags);
		return ok(result.render(query, mediaFilesCount, mediaFiles, prev, next));
	}
}
