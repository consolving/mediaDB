package jobs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import helpers.MediaFileHelper;
import helpers.SystemHelper;
import helpers.ThumbnailsHelper;
import models.MediaFile;
import models.Property;
import play.Logger;
import services.JobService;

public class CheckJob extends AbstractJob {
	final Logger.ALogger logger = Logger.of(this.getClass());
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String STORAGE_FILE_TEMPLATE = ROOT_DIR + File.separator + "storage" + File.separator + "%file%";

	public CheckJob() {
		super("CheckJob");
		this.cancellable = true;
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
		File file, oldFile;
		List<MediaFile> files = MediaFile.nextChecks(20);
		for (MediaFile mediaFile : files) {
			file = new File(STORAGE_FILE_TEMPLATE.replace("%file%", SystemHelper.getFoldersForName(mediaFile.checksum)));
			oldFile = new File(STORAGE_FILE_TEMPLATE.replace("%file%", mediaFile.checksum));
			if(file.exists() || oldFile.exists()) {
				try {
					Property p = mediaFile.getProperty("size");
					mediaFile.filesize = p != null ? p.getLongValue() : MediaFileHelper.getSize(file);
					BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
					mediaFile.created = MediaFileHelper.fileTimeToDate(attr.creationTime());
					ThumbnailsHelper.createThumbnail(mediaFile, "800x600");
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
				ThumbnailsHelper.deleteThumbnails(mediaFile);
				mediaFile.delete();
			}
		}		
	}
}
