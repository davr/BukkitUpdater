package de.enco.BukkitUpdater.Async.PacketManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

public class Search extends Thread {
	protected static final Logger console = Logger.getLogger("Minecraft");
	private Player player;
	private String regex;
	private final ThreadHelper th = new ThreadHelper();
	
	public Search(Player player, String regex) {
		this.player = player;
		this.regex = regex;
	}
	
	public void run() {
		try {
			String received = th.sendData("search:"+regex);
			if (!received.equalsIgnoreCase("false")) {
				/*
				 * If there are matches
				 * send it to the player
				 */
				String[] resultList = received.split("::");
				for (int i=0; i < resultList.length; i++) {
					String[] pi = resultList[i].split(":");
					if (pi[2].equalsIgnoreCase("none"))
						pi[2] = "No description found";
					th.sendTo(player, ChatColor.GREEN, pi[0]+" "+pi[1]+" - "+pi[2]);
				}
			} else {
				th.sendTo(player, ChatColor.RED, "No matches found.");
			}
		} catch (IOException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		}
	}
}
