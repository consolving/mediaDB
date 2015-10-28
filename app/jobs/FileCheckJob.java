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
import models.MediaFolder;
import models.Property;
import play.Logger;
import services.JobService;

public class FileCheckJob extends AbstractJob {
	final Logger.ALogger logger = Logger.of(this.getClass());
	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String STORAGE_FILE_TEMPLATE = ROOT_DIR + File.separator + "storage" + File.separator + "%file%";
	private final static Integer BATCH_SIZE = getValue("job.FileCheckJob.batchsize", 20);
	
	public FileCheckJob() {
		super("FileCheckJob");
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
		File file, newFile, oldFile;
		List<MediaFile> files = MediaFile.nextChecks(BATCH_SIZE);
		for (MediaFile mediaFile : files) {
			newFile = new File(STORAGE_FILE_TEMPLATE.replace("%file%", SystemHelper.getFoldersForName(mediaFile.checksum)));
			oldFile = new File(STORAGE_FILE_TEMPLATE.replace("%file%", mediaFile.checksum));
			if(newFile.exists() || oldFile.exists()) {
				try {
					file = new File(STORAGE_FILE_TEMPLATE.replace("%file%", mediaFile.getLocation()));
					Property p = mediaFile.getProperty("size");
					mediaFile.filesize = p != null ? p.getLongValue() : MediaFileHelper.getSize(file);
					p = mediaFile.getProperty("filename");
					mediaFile.folder = p != null ? MediaFolder.getOrCreate(getFolderFromFilename(p.v, mediaFile.filename)) : null;
					BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
					mediaFile.created = MediaFileHelper.fileTimeToDate(attr.creationTime());
					ThumbnailsHelper.createThumbnail(mediaFile, "800x600");
					MediaFileHelper.addDuration(mediaFile);
					MediaFileHelper.addDimensions(mediaFile);
				} catch (IOException ex) {
					logger.warn(ex.getLocalizedMessage(), ex);
				}
				mediaFile.checked();
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
	
	private final static String getFolderFromFilename(String path, String filename) {
		if(path != null && filename != null) {
			Logger.debug("path.length(): "+path.length());
			Logger.debug("filename.length(): "+filename.length());
			Logger.debug("path.substring(0, path.length()-filename.length()): "+path.substring(0, path.length()-filename.length()));
			return path.substring(0, path.length()-filename.length());
		}
		return null;
	}
}
