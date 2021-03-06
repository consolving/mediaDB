package helpers;

import java.io.File;

import com.typesafe.config.ConfigFactory;

import play.Logger;

public class OpensslHelper {
	private final static String SYSTEM_OPENSSL_BIN = ConfigFactory.load().getString("system.openssl.bin");
	private final static boolean HAS_OPENSSL = new File(SYSTEM_OPENSSL_BIN).exists();
	
	private OpensslHelper() {}
	
	public static String getSha256Checksum(File file) {
		if(HAS_OPENSSL && file.exists()){
			String cmd = SYSTEM_OPENSSL_BIN+" dgst -sha256 \""+file.getAbsolutePath()+"\"";
			Logger.debug("running: "+cmd);
			String[] parts =  SystemHelper.runCommand(cmd).split("=");
			return parts.length > 1 ? parts[parts.length-1].trim() : null;
		} else {
			Logger.warn(file.getAbsolutePath() + " does not exists!");
		}
		return null;
	}
	
	public static String getMd5Checksum(File file) {
		if(HAS_OPENSSL && file.exists()){
			String cmd = SYSTEM_OPENSSL_BIN+" dgst -md5 \""+file.getAbsolutePath()+"\"";
			Logger.debug("running: "+cmd);
			String[] parts =  SystemHelper.runCommand(cmd).split("=");
			return parts.length > 1 ? parts[parts.length-1].trim() : null;
		} else {
			Logger.warn(file.getAbsolutePath() + " does not exists!");
		}
		return null;
	}
}
