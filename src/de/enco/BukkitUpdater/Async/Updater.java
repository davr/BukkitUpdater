package de.enco.BukkitUpdater.Async;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

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

public class Updater extends Thread{
	private Plugin plugin;
	protected static final Logger console = Logger.getLogger("Minecraft");
	private final ThreadHelper th = new ThreadHelper();
	private ArrayList<String> supported = new ArrayList<String>();
	private ArrayList<Plugin> needUpdate = new ArrayList<Plugin>();
	private ArrayList<String> unsupported = new ArrayList<String>();
	
	FileConfiguration getExchange = null;
	
	public Updater(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public void run() {
		PluginManager pm = plugin.getServer().getPluginManager();
		Plugin[] plugins = pm.getPlugins();
		// for debugging mode
		if (th.debug()) console.log(Level.WARNING, "[DEBUG] Start update process ...");
		getExchange = YamlConfiguration.loadConfiguration(th.exchange);
		try {
			if (lookup(plugins)) {
				saveUpdateTime();
				// for debugging mode
				if (th.debug()) {
					console.log(Level.WARNING, "[DEBUG] Following plugins are up2date:");
					console.log(Level.WARNING, "[DEBUG] "+supported.toString());
					console.log(Level.WARNING, "[DEBUG] Following plugins need updates:");
					console.log(Level.WARNING, "[DEBUG] "+needUpdate.toString());
					console.log(Level.WARNING, "[DEBUG] Following were not found in database:");
					console.log(Level.WARNING, "[DEBUG] "+unsupported.toString());
				}
				for (int i=0; i < needUpdate.size(); i++) {
					String plugin = needUpdate.get(i).getDescription().getName();
					if (th.debug()) console.log(Level.WARNING, "[DEBUG] update: "+plugin);
					if (th.update(null, plugin)) {
						this.plugin.getServer().broadcastMessage(
								ChatColor.GREEN+"The plugin "+plugin+" was successfully updated."
						);
					} else {
						console.log(Level.WARNING, "The plugin "+plugin+" update failed!");
					}
				}
				// reload server plugins
				if (needUpdate.size() > 0) {
					if (th.debug()) console.log(Level.WARNING, "[DEBUG] Reloading server/plugin configurations ...");
					th.saveReload(plugin);
				}
				// Save unsupported plugins in the exchange file
				getExchange.load(th.exchange);
				getExchange.set("plugins.unsupported", unsupported);
				getExchange.save(th.exchange);
				// for debugging mode
				if (th.debug()) console.log(Level.WARNING, "[DEBUG] Update process finished!");
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
		} catch (ParseException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		}
	}
	
	public boolean lookup(Plugin[] plugins) throws IOException, InvalidConfigurationException{
		String data = "";
		String response;
		// we have to clear the lists first
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
				if (th.blacklist(plugins[i]))
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
	
	public void saveUpdateTime() throws FileNotFoundException, IOException, InvalidConfigurationException {
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
		getExchange = YamlConfiguration.loadConfiguration(th.exchange);
		
		ArrayList<String> lastUpdate = new ArrayList<String>(supported.size());
		for (int i=0; i < supported.size(); i++) {
			lastUpdate.add(i, supported.get(i)+" last update "+timestamp.toString());
		}
		getExchange.load(th.exchange);
		getExchange.set("plugins.updated", lastUpdate);
		getExchange.save(th.exchange);
	}
}