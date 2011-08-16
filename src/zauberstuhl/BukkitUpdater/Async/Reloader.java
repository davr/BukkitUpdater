package zauberstuhl.BukkitUpdater.Async;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

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

public class Reloader extends Thread {
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
			new Debugger(player, e.getMessage(), "(Something went wrong)");
		}
	}
}
