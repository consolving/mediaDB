package jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import helpers.MediaFileHelper;
import models.MediaFile;
import play.Logger;
import services.JobService;

public class CheckJob extends AbstractJob {
	final Logger.ALogger logger = Logger.of(this.getClass());
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String STORAGE_FILE_TEMPLATE = ROOT_DIR + File.separator + "storage" + File.separator + "%file%";

	public CheckJob() {
		super("CheckJob");
	}

	@Override
	public void run() {
		if(JobService.isJobActive(getName())) {
			JobService.setLastRun(getName());
			checkNext();
		} else if(jobHandler != null){		
			jobHandler.stop();
		}		
	}
	
	private void checkNext() {
		File file;
		List<MediaFile> files = MediaFile.Finder.setMaxRows(20).order("lastCheck ASC").findList();
		for (MediaFile mediaFile : files) {
			file = new File(STORAGE_FILE_TEMPLATE.replace("%file%", mediaFile.checksum));
			if(file.exists()) {
				try {
					BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
					mediaFile.created = MediaFileHelper.fileTimeToDate(attr.creationTime());
					mediaFile.checked();
				} catch (IOException ex) {
					logger.warn(ex.getLocalizedMessage(), ex);
				}
			} else {
				logger.info(mediaFile.toString() + ": file not found!");
				mediaFile.deleteManyToManyAssociations("tags");
				for(models.Property prop : mediaFile.getProperties()) {
					prop.delete();
				}
				mediaFile.delete();
			}
		}		
	}
}
