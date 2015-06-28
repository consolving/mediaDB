package controllers;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import com.typesafe.config.ConfigFactory;

import helpers.OpensslHelper;
import models.MediaFile;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

	private final static String ROOT_DIR = ConfigFactory.load().getString("media.root.dir");
	
    public static Result index() {
    	File mediaFolder = new File(ROOT_DIR+File.separator+"upload");
    	String checksum = "";
    	Set<File> mediaFiles = new TreeSet<File>();
    	if(mediaFolder.exists()){
    		for(File f : mediaFolder.listFiles()){
    			checksum = OpensslHelper.getSha256Checksum(f);
    			MediaFile mf = MediaFile.Finder.where().eq("checksum", checksum).findUnique();
    			if(checksum != null && mf == null) {
    				mf = new MediaFile();
    				mf.checksum = checksum;
    				mf.filename = f.getName();
    				mf.save();
    				Logger.debug("created "+f.getAbsolutePath()+" checksum " + checksum);
    			} else {
    				Logger.debug(f.getAbsolutePath()+" already found!");
    			}
    		}
    	}
        return ok(index.render("Media DB", mediaFiles));
    }

}
