package de.enco.BukkitUpdater.Async;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
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

public class Reloader extends Thread {
	protected static final Logger console = Logger.getLogger("Minecraft");
	private final ThreadHelper th = new ThreadHelper();
	private Player player;
	private PluginManager pm;
	private Plugin plugin;
	
	public Reloader(Player player, PluginManager pm, Plugin plugin) {
		this.player = player;
		this.pm = pm;
		this.plugin = plugin;
	}
	
	public void run() {
		if (plugin.getDescription().getName().equalsIgnoreCase("BukkitUpdater")) {
			console.log(Level.WARNING, "BukkitUpdater cannot restart on his own. Please reload the server ...");
			return;
		}
		
		try {
			pm.disablePlugin(plugin);
			if (!pm.isPluginEnabled(plugin)) {
				pm.enablePlugin(plugin);
				if (pm.isPluginEnabled(plugin)) {
					th.sendTo(player, "GREEN", "["+plugin.getDescription().getName()+"] version "+plugin.getDescription().getVersion() + " enabled.");
					return;
				} else {
					th.sendTo(player, "RED", "["+plugin.getDescription().getName()+"] Failed to re-enable.");
					return;
				}
			} else {
				th.sendTo(player, "RED", "["+plugin.getDescription().getName()+"] Failed to disable.");
				return;
			}
		} catch (NullPointerException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		}
	}
}
