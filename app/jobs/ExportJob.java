package jobs;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.typesafe.config.ConfigFactory;

import helpers.SystemHelper;
import models.MediaFile;
import play.Logger;

public class ExportJob extends AbstractJob {

    private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
    private final static File EXPORT_FOLDER = new File(ROOT_DIR + File.separator + "export");
    private final static String STORAGE_FOLDER = ROOT_DIR + File.separator + "storage";
    private int batchSize;

    public ExportJob(int batchSize) {
        super("Exporting " + batchSize + " files. / " + new Date());
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
        Logger.info("exporting {} files.", batchSize);
        List<MediaFile> files = MediaFile.nextExports(batchSize);
        int count = 1;
        for(MediaFile mediaFile : files) {
            final File oldPath = new File(STORAGE_FOLDER + File.separator + mediaFile.getLocation());
            final String newFolder = EXPORT_FOLDER + mediaFile.getFolderName();
            checkFolder(newFolder);
            final File newPath = new File(newFolder + mediaFile.filename);
            final boolean flag = SystemHelper.move(oldPath, newPath);
            if(flag) {
                mediaFile.filepath = "exported";
                mediaFile.update();
                Logger.info("{} {} was successfully exported to {}", 
                        count,
                        mediaFile.checksum, 
                        newPath.getAbsolutePath());
            }
            count++;
        }
        Logger.info("{} is done!", this.getName());
    }

    private void checkFolder(String folderName) {
        File folder = new File(folderName);
        if(!folder.exists()) {
            folder.mkdirs();
        }
    }
}
