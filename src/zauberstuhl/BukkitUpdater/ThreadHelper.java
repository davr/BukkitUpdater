package zauberstuhl.BukkitUpdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
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
	
	public String sendData(String cwd, String send){
		String token = "";
		String received = null;
		File txt = new File(cwd+"/plugins/BukkitUpdater/token.txt");
		
		if (txt.exists()) {
			token = readFile(txt);
		}
		
		try {
			URL adress = new URL( "http://mc.zauberstuhl.de/bukkit_updater/index.pl?s="+send+"&t="+token );
			InputStream in = adress.openStream();
			received = new Scanner( in ).useDelimiter( "\\Z" ).next();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return received;
	}
	
	public void helper(Player player) {
		sendTo(player, "RED", "Bukkit Updater Commands:");
		sendTo(player, "WHITE", "");
		sendTo(player, "GOLD", "/u2d");
		sendTo(player, "WHITE", "Shows outdated plugin(s)");
		sendTo(player, "GOLD", "/u2d update <PluginName>");
		sendTo(player, "WHITE", "Update the plugin if there is following tag behind the name \"(L)\"");
		sendTo(player, "GOLD", "/u2d unsupported");
		sendTo(player, "WHITE", "Shows unsupported plugins");
		sendTo(player, "GOLD", "/u2d help");
		sendTo(player, "WHITE", "Display this help-text");
	}

	public String readFile(File file) {
		String s;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			try {
				s = br.readLine();
				return s;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void writeToFile(File file, String input) {
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(input);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
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
