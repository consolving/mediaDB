package jobs;

import java.io.File;
import java.io.FileFilter;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.ConfigFactory;

import helpers.MediaFileHelper;
import play.cache.Cache;
import play.libs.Json;
import services.JobService;

public class FolderSizesJob extends AbstractJob {
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static FileFilter FOLDER_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.isDirectory() && !pathname.getName().startsWith(".");
		}
	};

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
			out = Json.newObject();
			File rootFolder = new File(ROOT_DIR);
			ArrayNode dirSizes = out.arrayNode();
			ArrayNode dirCounts = out.arrayNode();
			if (rootFolder.exists()) {
				Long sum = MediaFileHelper.getSize(rootFolder);
				Long part = 0L;
				for (File folder : rootFolder.listFiles(FOLDER_FILTER)) {
					part = MediaFileHelper.getSize(folder);
					if (part != null && sum != null) {
						ObjectNode dir = Json.newObject();
						dir.put("label", folder.getName() + " " + MediaFileHelper.humanReadableByteCount(part * 1000, true));
						dir.put("value", 100 * part / sum);
						dirSizes.add(dir);

						part = MediaFileHelper.getCount(folder);
						dir = Json.newObject();
						dir.put("label", folder.getName() + " " + part);
						dir.put("value", part);
						dirCounts.add(dir);
					}
				}
			}
			out.put("dirsSizes", dirSizes);
			out.put("dirsCounts", dirCounts);
			Cache.set("folderStats", out, 120);
		}
	}
}
