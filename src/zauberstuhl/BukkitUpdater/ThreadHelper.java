package zauberstuhl.BukkitUpdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.entity.Player;

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

public class ThreadHelper {
	public ColouredConsoleSender console = new ColouredConsoleSender(
			(CraftServer) Bukkit.getServer());
	// current working directory
	public String cwd = System.getProperty("user.dir");
	// token for adding static links
	public File token = new File(cwd+"/plugins/BukkitUpdater/token.txt");
	// you can hide plugins with that list
	public File blacklist = new File(cwd+"/plugins/BukkitUpdater/blacklist.txt");
	// will log all the debug information
	public File debugLog = new File(cwd+"/plugins/BukkitUpdater/debug.log");
	// main folder
	public File folder = new File(cwd +"/plugins/BukkitUpdater/");
	// backup folder
	public File backupFolder = new File(cwd +"/plugins/BukkitUpdater/backup/");
	
	public String sendData(String send) throws IOException{
		String token = "";
		
		if (this.token.exists()) {
			token = readFile(this.token);
		}
		URL adress = new URL( "http://mc.zauberstuhl.de/bukkit_updater/lookup.pl?s="+send+"&t="+token );
		InputStream in = adress.openStream();
		return new Scanner( in ).useDelimiter( "\\Z" ).next();
	}
	
	public void helper(Player player) {
		sendTo(player, "RED", "Bukkit Updater Commands:");
		sendTo(player, "WHITE", "");
		sendTo(player, "GOLD", "/u2d");
		sendTo(player, "WHITE", "Shows outdated plugin(s)");
		sendTo(player, "GOLD", "/u2d update <PluginName>");
		sendTo(player, "WHITE", "Update the plugin if there is following tag behind the name \"(L)\"");
		sendTo(player, "GOLD", "/u2d reload <PluginName>");
		sendTo(player, "WHITE", "Reload the plugin e.g. after a update");
		sendTo(player, "GOLD", "/u2d unsupported");
		sendTo(player, "WHITE", "Shows unsupported plugins");
		sendTo(player, "GOLD", "/u2d help");
		sendTo(player, "WHITE", "Display this help-text");
	}

	public String readFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		return br.readLine();
	}

	public void writeToFile(File file, String input) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		out.write(input);
		out.close();
	}
	
	public void sendTo(Player player, String color, String string) {
		if (player == null) {
			if (!string.equalsIgnoreCase(""))
				console.sendMessage(ChatColor.valueOf(color)+string);
		} else {
			player.sendMessage(ChatColor.valueOf(color)+string);
		}
	}
}
