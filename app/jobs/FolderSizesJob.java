package jobs;

import helpers.MediaFileHelper;
import play.cache.Cache;
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
		ObjectNode out = (ObjectNode) Cache.get("folderStats");
		if (out == null) {
			Cache.set("folderStats", MediaFileHelper.getFolderSizes(), 3600);
		}
	}
}
