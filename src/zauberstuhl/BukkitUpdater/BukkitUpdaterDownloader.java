package zauberstuhl.BukkitUpdater;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
* BukkitUpdater for Bukkit
* @author zauberstuhl
*/

public class BukkitUpdaterDownloader {
	private final BukkitUpdater plugin;
	public BukkitUpdaterDownloader(BukkitUpdater instance) {
		plugin = instance;
	}
	
	public boolean update(String pluginName) throws IllegalStateException, MalformedURLException, ProtocolException, IOException {
		OutputStream os;
		String dtFile = plugin.cwd+"/plugins/"+pluginName+".jar";
		String url = plugin.sendData(pluginName+"::url");
		
		if (url.equalsIgnoreCase("false")) {
			return false;
		}
		
		backup(dtFile, plugin.cwd+"/plugins/BukkitUpdater/backup/"+pluginName+".jar");
		os = new FileOutputStream(dtFile);
		downloadFile(url, os);
		return true;
	}
		
	public static void downloadFile(String url_str, OutputStream os)
	throws IllegalStateException, MalformedURLException,
	ProtocolException, IOException {
		URL url = new URL(url_str.replace(" ", "%20"));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
        	byte tmp_buffer[] = new byte[4096];
            InputStream is = conn.getInputStream();
            int n;
            while ((n = is.read(tmp_buffer)) > 0) {
            	os.write(tmp_buffer, 0, n);
            	os.flush();
            }
        } else {
        	throw new IllegalStateException("HTTP response: " + responseCode);
        }
	}
	
	public static void backup(String srFile, String dtFile){
		InputStream in;
		try {
			in = new FileInputStream(new File(srFile));
			OutputStream out = new FileOutputStream(new File(dtFile));
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
