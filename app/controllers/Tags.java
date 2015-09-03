package controllers;

import java.util.List;

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
	
	public static Result show(String tagName) {
		if(tagName == null) {
			return redirect(routes.Application.index());
		}
		Tag tag = Tag.findOrCreateTagByName(tagName.trim().toLowerCase());
		List<MediaFile> mediaFiles = tag.getMediaFiles();
		return ok(show.render(tagName, mediaFiles));
	}
	
}
