package de.enco.BukkitUpdater.Async;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.esotericsoftware.wildcard.Paths;

import de.enco.BukkitUpdater.ThreadHelper;

/**
* BukkitUpdater 2.0.x
* Copyright (C) 2011 Lukas Matt 'zauberstuhl y33' <lukas@zauberstuhl.de>
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

public class Repeater extends Thread{
	protected static final Logger console = Logger.getLogger("Minecraft");
	private final ThreadHelper th = new ThreadHelper();
	private Plugin bu;
	private ArrayList<String> supported = new ArrayList<String>();
	private ArrayList<Plugin> needUpdate = new ArrayList<Plugin>();
	private ArrayList<String> unsupported = new ArrayList<String>();
	
	public Repeater(Plugin bu) {
		this.bu = bu;
	}
	
	public void run() {
		//debug
		//console.log(Level.WARNING, "[DEBUG] STARTING UPDATE MODE");
		PluginManager pm = bu.getServer().getPluginManager();
		Plugin[] plugins = pm.getPlugins();
		try {
			if (lookup(plugins)) {
				//debug
				//console.log(Level.WARNING, "[DEBUG] supported: "+supported.toString());
				//console.log(Level.WARNING, "[DEBUG] needUpdate: "+needUpdate.toString());
				//console.log(Level.WARNING, "[DEBUG] unsupported: "+unsupported.toString());
				saveUpdateTime();
				for (int i=0; i < needUpdate.size(); i++) {
					String plugin = needUpdate.get(i).getDescription().getName();
					//console.log(Level.WARNING, "[DEBUG] update: "+plugin);
					if (update(plugin)) {
						bu.getServer().broadcastMessage(ChatColor.GREEN+"The plugin "+plugin+" was successfully updated.");
						if (reload(bu, pm, pm.getPlugin(plugin)))
							bu.getServer().broadcastMessage(ChatColor.GREEN+"The plugin "+plugin+" was successfully reloaded.");
						else
							bu.getServer().broadcastMessage(ChatColor.RED+"Please reload the plugin "+plugin+" manually!");
					} else {
						console.log(Level.WARNING, "The plugin "+plugin+" update failed!");
					}
				}
				// Save unsupported plugins in the exchange file
				FileConfiguration config = bu.getConfig();
				config.load(th.exchange);
				config.set("plugins.unsupported", unsupported);
				config.save(th.exchange);
			}
		} catch (IllegalStateException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (MalformedURLException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (ProtocolException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (IOException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (InvalidConfigurationException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		}
	}
	
	public boolean update(String pluginName) throws IOException {
		String requestedUrl = th.sendData(pluginName+":url");
		//console.log(Level.WARNING, "[DEBUG] link = "+pluginName+":"+url);
		if (requestedUrl.equalsIgnoreCase("false")
				|| requestedUrl.equalsIgnoreCase("")) {
			return false;
		}
		/*
		 * First of all backup
		 * the old versions
		 */
		backup(pluginName);
		
		OutputStream os = new FileOutputStream(th.cwd+"/plugins/"+pluginName+".jar");
		URL url = new URL(requestedUrl.replace(" ", "%20"));
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
            return true;
        } else {
        	console.log(Level.WARNING, "HTTP response: " + responseCode);
        	return false;
        }
	}
	
	public void backup(String plugin) throws IOException{
		Paths paths = new Paths();
		paths.regex(th.cwd+"/plugins", ".*?"+plugin+".*?\\.jar");
		
		for (int i=0; i < paths.getPaths().size(); i++) {
			InputStream in = new FileInputStream(
					new File(paths.getPaths().get(i)));
			OutputStream out = new FileOutputStream(
					new File(th.cwd+"/plugins/BukkitUpdater/backup/"+plugin+".jar.backup"));
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
		// delete old versions
		paths.delete();
	}
	
	public boolean lookup(Plugin[] plugins) throws IOException, InvalidConfigurationException{
		String data = "";
		String response;
		// we have to clear the list first
		supported.clear();
		needUpdate.clear();
		unsupported.clear();
		for(int i = 0; i < plugins.length; i++){
			String version = plugins[i].getDescription().getVersion();
			String name = plugins[i].getDescription().getName();
			data += name+":"+version+"::";
		}
		response = th.sendData(data);

		String []result = response.split(":");
		for(int i = 0; i < result.length; i++) {
			int x = Integer.parseInt(result[i]);
			/*
			 * compare version with database
			 * 0=needs update; 1=is up2date; 2=unsupported
			 */
			if (x == 0) {
				supported.add(plugins[i].toString());
				if (blacklist(plugins[i]))
					needUpdate.add(plugins[i]);
			} else if (x == 1) {
				supported.add(plugins[i].toString());
			} else if (x == 2) {
				unsupported.add(plugins[i].toString());
			}
		}
		// are there updates?
		if(!response.matches("1"))
			return true;
		return false;
	}
	
	public static boolean reload(Plugin bu, PluginManager pm, Plugin plugin) {
		//if (plugin.getDescription().getName().equalsIgnoreCase("BukkitUpdater")) {
		//	console.log(Level.WARNING, "BukkitUpdater cannot restart on his own. Please reload the server ...");
		//	return false;
		//}
		// now try to reload
		pm.disablePlugin(plugin);
		if (!pm.isPluginEnabled(plugin)) {
			pm.enablePlugin(plugin);
			if (pm.isPluginEnabled(plugin))
				return true;
			else
				return false;
		} else
			return false;
	}
	
	public void saveUpdateTime() throws FileNotFoundException, IOException, InvalidConfigurationException {
		Calendar currentDate = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss dd/mm/yyyy");
		String dateNow = formatter.format(currentDate.getTime());
		
		ArrayList<String> lastUpdate = new ArrayList<String>(supported.size());
		for (int i=0; i < supported.size(); i++) {
			lastUpdate.add(i, supported.get(i)+" last update "+dateNow);
		}
		FileConfiguration config = bu.getConfig();
		config.load(th.exchange);
		config.set("plugins.updated", lastUpdate);
		config.save(th.exchange);
	}
	
	public boolean blacklist(Plugin plugin) throws FileNotFoundException, IOException, InvalidConfigurationException {
		FileConfiguration config = bu.getConfig();
		
		config.load(th.config);
		List plugins = config.getList("plugins.blacklist");
		if (plugins.contains(plugin))
			return false;
		return true;
	}
}