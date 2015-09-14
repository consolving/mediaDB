package jobs;

import helpers.MediaFileHelper;
import play.cache.Cache;
import play.libs.Json;
import services.JobService;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class FolderSizesJob extends AbstractJob {

	public FolderSizesJob() {
		super("FolderSizesJob");
	}

	@Override
	public void run() {
		if (JobService.isJobActive(getName())) {
			JobService.setLastRun(getName());
			collectFolderSizes();
		} else if (jobHandler != null) {
			jobHandler.stop();
		}
	}

	private void collectFolderSizes() {
		ObjectNode out = MediaFileHelper.addFolderSizes(Json.newObject());
		out = MediaFileHelper.addCounts(out);
		out = MediaFileHelper.addTypeCounts(out);
		Cache.set("folderStats", out, 60*10);
	}
}
