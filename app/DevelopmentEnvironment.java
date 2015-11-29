import java.io.File;
import java.io.FileFilter;

import com.typesafe.config.ConfigFactory;

import helpers.SystemHelper;
import play.Application;

public class DevelopmentEnvironment {
	private final static String DATA_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String TEST_DATA_DIR = DATA_DIR+File.separator+"test";
	private final static String[] SUB_FOLDER = {"storage", "thumbnails", "upload"};
	public DevelopmentEnvironment(Application app) {
	}
	
	public void init() {
		File testDir = new File(TEST_DATA_DIR);
		File uploadsFolder = new File(DATA_DIR+File.separator+"upload");
		if(testDir.exists()) {
			File folder;
			for(String f : SUB_FOLDER) {
				folder = new File(DATA_DIR+File.separator+f);
				if(folder.exists() && folder.listFiles().length > 0) {
					for(File file : folder.listFiles(new FileFilter() {		
						@Override
						public boolean accept(File pathname) {
							return !pathname.getName().startsWith(".");
						}
					})){
						SystemHelper.delete(file);
					}
				}
			}
			
			for(File file : testDir.listFiles(new FileFilter() {		
				@Override
				public boolean accept(File pathname) {
					return !pathname.getName().startsWith(".");
				}
			})){
				SystemHelper.copy(file, uploadsFolder);
			}
		}
	}
}
