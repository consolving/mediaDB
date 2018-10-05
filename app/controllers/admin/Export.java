package controllers.admin;

import fileauth.actions.BasicAuth;
import jobs.ExportJob;
import play.mvc.Controller;
import play.mvc.Result;

@BasicAuth
public class Export extends Controller {
    private final static Integer BATCH_SIZE = 500;

    public static Result start() {
        ExportJob ej = new ExportJob(BATCH_SIZE);
        ej.run();
        return Dashboard.index();
    }
}
