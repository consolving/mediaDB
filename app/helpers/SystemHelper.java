package helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import com.typesafe.config.ConfigFactory;

import play.Logger;

public class SystemHelper {
    private final static String MV_BIN = ConfigFactory.load().getString("system.mv.bin");
    private final static boolean HAS_MV_BIN = new File(MV_BIN).exists();
    private final static String RM_BIN = ConfigFactory.load().getString("system.rm.bin");
    private final static boolean HAS_RM_BIN = new File(RM_BIN).exists();
    private final static String CP_BIN = ConfigFactory.load().getString("system.cp.bin");
    private final static boolean HAS_CP_BIN = new File(CP_BIN).exists();
    
    private final static String SYSTEM_bash_BIN = ConfigFactory.load().getString("system.bash.bin");

    private SystemHelper() {
    }

    public static String runCommand(String cmd) {
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder pb = new ProcessBuilder(SYSTEM_bash_BIN, "-c", cmd);
            String line;
            Process p = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line != null && line.toLowerCase().trim().contains("error")) {
                    Logger.error(line.trim());
                }
                sb.append(line.trim()).append("\n");
            }
            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }
        return sb.toString();
    }

    public static String getFoldersForName(String name) {
        return calcFolder(name, true);
    }

    public static String getFolders(String name) {
        return calcFolder(name, false);
    }

    public static boolean copy(File src, File dst) {
        if (HAS_CP_BIN && src.exists() && (dst.isDirectory() && dst.exists() || dst.isFile())) {
            String cmd = dst.isFile() ? CP_BIN + " -r \"" + src.getAbsolutePath() + "\" \"" + dst.getAbsolutePath() + "\"" : CP_BIN + " -r \"" + src.getAbsolutePath() + "\" \"" + dst.getAbsolutePath() + "/\"";
            Logger.debug("running: " + cmd);
            String part = SystemHelper.runCommand(cmd).trim();
            Logger.debug(part);
            File copy = new File(dst.getAbsolutePath()+File.separator+src.getName());
            return copy.exists();
        }
        return false;
    }
    
    public static boolean delete(File file) {
        if (HAS_RM_BIN && file.exists()) {
            String cmd = RM_BIN + " -Rf \"" + file.getAbsolutePath() + "/\"";
            Logger.debug("running: " + cmd);
            String part = SystemHelper.runCommand(cmd).trim();
            Logger.debug(part);
            return !file.exists();
        }
        return false;
    }

    public static boolean delete(String filename) {
        File file = new File(filename);
        return delete(file);
    }

    public static boolean move(File from, File to) {
        if (HAS_MV_BIN && from.exists()) {
            String cmd = MV_BIN + 
                    " \"" + from.getAbsolutePath() + "\" "
                    + "\"" + to.getAbsolutePath() + "\" ";
            Logger.debug("running: " + cmd);
            String part = SystemHelper.runCommand(cmd).trim();
            Logger.debug(part);
            return to.exists();
        }
        return false;
    }

    private static String calcFolder(String name, boolean file) {
        if (name == null) {
            return name;
        }
        int l = name.length();
        int div = 8;
        int count = l / div;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= count; i++) {
            sb.append(name.substring((i - 1) * div, i * div));
            sb.append(i != count ? File.separator : "");
        }
        if (file) {
            sb.append(File.separator);
            sb.append(name);
        }
        return sb.toString();
    }
}
