package controllers.admin;

import fileauth.actions.BasicAuth;
import models.MediaFile;
import models.Property;
import models.Tag;
import models.Thumbnail;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.admin.Dashboard.index;

@BasicAuth
public class Dashboard extends Controller {
    public static Result index() {
        int mediaFileCount = MediaFile.Finder.findRowCount();
        int tagCount = Tag.Finder.findRowCount();
        int propertyCount = Property.Finder.findRowCount();
        int thumbnailsCount = Thumbnail.Finder.findRowCount();
        return ok(index.render(mediaFileCount, thumbnailsCount, tagCount, propertyCount));
    }
}
