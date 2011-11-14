package de.enco.BukkitUpdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.esotericsoftware.wildcard.Paths;

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

public class ThreadHelper {
	private Plugin plugin;
	protected static final Logger console = Logger.getLogger("Minecraft");
	
	public File root = new File(System.getProperty("user.dir"));
	public File cwd = new File(root, "/plugins/BukkitUpdater/");
	public File config = new File(cwd, "config.yml");
	public File exchange = new File(cwd, "data.yml");
	public File backupFolder = new File(cwd, "backup/");
	
	FileConfiguration getConfig = null;
	FileConfiguration getExchange = null;
	
	public ThreadHelper(Plugin plugin) {
		this.plugin = plugin;
	}
	
	/*
	 * Send a request and receive plugin informations
	 */
	public String sendData(String send) throws IOException {
		String received = "";
		String inputLine;
		
		URL adress = new URL( "http://bukkit.3nc0.de/request.pl?s="+send );
		BufferedReader in = new BufferedReader(
				new InputStreamReader(
						adress.openStream()
				)
		);
		while ((inputLine = in.readLine()) != null)
			received += inputLine;
		in.close();
		return received;
	}

	/*
	 * Update the defined plugin
	 */
	public boolean update(String pluginName) throws IOException {
		String requestedUrl = sendData(pluginName+":url");
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
		
		OutputStream os = new FileOutputStream(root+"/plugins/"+pluginName+".jar");
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
	
	/*
	 * Backup the defined plugin
	 */
	public void backup(String plugin) throws IOException{
		Paths paths = new Paths();
		paths.regex(root+"/plugins", ".*?"+plugin+".*?\\.jar");
		
		for (int i=0; i < paths.getPaths().size(); i++) {
			InputStream in = new FileInputStream(
					new File(paths.getPaths().get(i))
			);
			OutputStream out = new FileOutputStream(
					new File(backupFolder+plugin+".jar.backup")
			);
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
	
	/*
	 * Try to reload the server plugins/configuration
	 * Success depends on the last time the server was reloaded 
	 */
	public void reloadingServer() throws FileNotFoundException, IOException, InvalidConfigurationException, ParseException {
		Date date = new Date();
		Timestamp timestamp = new Timestamp(date.getTime());
				
		getExchange = YamlConfiguration.loadConfiguration(exchange);
		if (getExchange.getString("scheduler.reload.last") == null) {
			getExchange.set(
					"scheduler.reload.last",  SimpleDateFormat.getInstance().format(
							timestamp
					)
			);
			getExchange.save(exchange);
		}
		Timestamp lastReload = new Timestamp(
				SimpleDateFormat.getInstance().parse(
						getExchange.getString(
								"scheduler.reload.last"
						)
				).getTime()
		);
		/*
		 * Wait min. 25 minutes
		 * unless you reload the server again
		 */
		if ((timestamp.getMinutes() - lastReload.getMinutes()) >= 25) {
			plugin.getServer().reload();
			plugin.getServer().broadcastMessage(
					ChatColor.GREEN+"The updated plugins were successfully reloaded."
			);
		} else {
			if (debug()) console.log(Level.WARNING, "[DEBUG] BukkitUpdater can only reload the server every 25 minutes. Reloading stopped!!");
		}
	}
	
	/*
	 * Display the help page to a specific player
	 */
	public void helper(Player player) {
		sendTo(player, "RED", "Bukkit Updater Commands:");
		sendTo(player, "WHITE", "");
		sendTo(player, "GOLD", "/u2d - Shows updated plugin(s)");
		sendTo(player, "GOLD", "/u2d update - Will trigger manual the update process");
		sendTo(player, "GOLD", "/u2d ignore list - List all ignored plugins");
		sendTo(player, "GOLD", "/u2d ignore <PluginName> - Add/Remove a plugin from the blacklist");
		//sendTo(player, "GOLD", "/u2d unsupported - Shows unsupported plugins");
		sendTo(player, "GOLD", "/u2d help - Display this help-text");
	}
	
	/*
	 * Returns true if the plugin name
	 * doesn't match the blacklist
	 */
	public boolean blacklist(Plugin plugin) throws FileNotFoundException, IOException, InvalidConfigurationException {
		getConfig = YamlConfiguration.loadConfiguration(config);
		List plugins = getConfig.getList("plugins.blacklist");
		if (plugins.contains(plugin))
			return false;
		return true;
	}
	
	/*
	 * Send a message to a specific player
	 */
	public void sendTo(Player player, String color, String string) {
		if (player == null) {
			if (!string.equalsIgnoreCase(""))
				console.log(Level.INFO, string);
		} else {
			player.sendMessage(ChatColor.valueOf(color)+string);
		}
	}
	
	/*
	 * Returns true if debugging is enabled
	 * in the configuration file
	 */
	public boolean debug() {
		getConfig = YamlConfiguration.loadConfiguration(config);
		return getConfig.getBoolean("debug");
	}
}