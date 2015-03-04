package controllers;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import com.typesafe.config.ConfigFactory;

import play.api.libs.Codecs;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	private final static String FFMPEG_BIN = ConfigFactory.load().getString("media.ffmpeg.bin"); 
	private final static String FFPROBE_BIN = ConfigFactory.load().getString("media.ffprobe.bin"); 
	private final static String SYSTEM_OPENSSL_BIN = ConfigFactory.load().getString("system.openssl.bin"); 
    public static Result index() {
    	File mediaFolder = new File(ROOT_DIR+File.separator+"upload");
    	Set<File> mediaFiles = new TreeSet<File>();
    	if(mediaFolder.exists()){
    		for(File f : mediaFolder.listFiles()){
    			mediaFiles.add(f);
    		}
    	}
        return ok(index.render("Media DB", mediaFiles));
    }

}
