package models;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.typesafe.config.ConfigFactory;

import play.Logger;
import play.db.ebean.Model;

@Entity
public class MediaFolder extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    
    @Lob
    @Column(columnDefinition = "TEXT")    
    public String path;
    public String name;
    @ManyToOne
    public MediaFolder parent = null;
    @OneToMany(mappedBy="folder")
    public List<MediaFile> files = new ArrayList<MediaFile>();
    @OneToMany(mappedBy="parent")
    public List<MediaFolder> folders = new ArrayList<MediaFolder>();

    public static Finder<Long, MediaFolder> Finder = new Finder<Long, MediaFolder>(Long.class, MediaFolder.class);

    private final static String UPLOAD_DIR = ConfigFactory.load().getString("media.root.dir") + File.separator + "upload";
    private final static File UPLOAD_DIR_HANDLE = new File(UPLOAD_DIR);

    public static MediaFolder getOrCreate(String path) {
        if(path == null || path.isEmpty()) {
            return null;
        }
        path = path.trim();
        Logger.debug("path "+path);
        MediaFolder mf = MediaFolder.Finder.where().eq("path", path).findUnique();
        if(mf == null) {
            mf = new MediaFolder();
            mf.name = getName(path);
            mf.path = path;
            if(!File.separator.equals(mf.name)) {
                String mediaFolder = getFolder(path);
                Logger.info("creating mediaFolder "+mediaFolder);
                mf.parent = MediaFolder.getOrCreate(mediaFolder);
            }
            mf.save();
        }
        return mf;
    }
    
    private final static String getName(String path) {
        String[] parts = path.trim().split(File.separator);
        return parts.length > 0 ? parts[parts.length-1] : path;
    }
    
    private final static String getFolder(String path) {
        String[] parts = path.trim().split(File.separator);
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< parts.length-1; i++){
            sb.append(parts[i]).append(File.separator);
        }
        return sb.toString();
    }

    public String toString() {
        return path.replace(UPLOAD_DIR_HANDLE.getAbsolutePath(), "").trim();
    }
}
