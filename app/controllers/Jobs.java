package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import services.JobService;

public class Jobs extends Controller {
	
	public static Result toggleJobActive(String jobname) {
		JobService.toggleJobActive(jobname);
		return redirect(routes.Application.index());
	}

	public static Result toggleJobsActive() {
		JobService.toggleJobsActive();
		return redirect(routes.Application.index());
	}
}
