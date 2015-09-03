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
		Tag tag = Tag.findOrCreateTagByName(tagName.trim());
		List<MediaFile> mediaFiles = tag.mediaFiles;
		return ok(show.render(tagName, mediaFiles));
	}
	
}
