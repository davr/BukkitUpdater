package zauberstuhl.BukkitUpdater.Async;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.bukkit.entity.Player;

import zauberstuhl.BukkitUpdater.BukkitUpdater;
import zauberstuhl.BukkitUpdater.ThreadHelper;

/**
* BukkitUpdater 0.2.x
* Copyright (C) 2011 Lukas 'zauberstuhl y33' Matt <lukas@zauberstuhl.de>
* and many thanks to V10lator for your support.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Permissions Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Permissions Public License for more details.
*
* You should have received a copy of the GNU Permissions Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/**
* This plugin was written for Craftbukkit
* @author zauberstuhl
*/

public class Downloader extends Thread{
	private final BukkitUpdater plugin = new BukkitUpdater();
	private final ThreadHelper th = new ThreadHelper();
	private Player player;
	private String pluginName;
	
	public Downloader(Player player, String pluginName) {
		this.pluginName = pluginName;
		this.player = player;
	}
	
	public void run() {
		try {
			if (update(pluginName)) {
				th.sendTo(player, "GREEN", "The plugin "+pluginName+" was successfully updated :)");
				th.sendTo(player, "GREEN", "Please reload the server now via /reload");
			} else {
				th.sendTo(player, "RED", "The plugin "+pluginName+" update failed!");
				th.sendTo(player, "RED", "You can update only plugins with the (L) behind the name.");
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean update(String pluginName) throws IllegalStateException, MalformedURLException, ProtocolException, IOException {
		OutputStream os;
		String dtFile = plugin.cwd+"/plugins/"+pluginName+".jar";
		String url = th.sendData(plugin.cwd, pluginName+"::url");
		
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
	
	public static void backup(String srFile, String dtFile) throws IOException{
		InputStream in;
		in = new FileInputStream(new File(srFile));
		OutputStream out = new FileOutputStream(new File(dtFile));
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}
}
