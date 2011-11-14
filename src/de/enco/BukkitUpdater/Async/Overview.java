package de.enco.BukkitUpdater.Async;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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

public class Overview extends Thread {
	private Plugin plugin;
	private final ThreadHelper th = new ThreadHelper(plugin);
	protected static final Logger console = Logger.getLogger("Minecraft");
	private Player player;
	
	FileConfiguration getExchange = null;
	
	public Overview(Player player, Plugin plugin) {
		this.player = player;
		this.plugin = plugin;
	}
	
	public void run() {
		getExchange = YamlConfiguration.loadConfiguration(th.config);
		try {
			getExchange.load(th.exchange);
		} catch (FileNotFoundException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (IOException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (InvalidConfigurationException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		}
		List plugins = getExchange.getList("plugins.updated");
		if (plugins.isEmpty()) {
			th.sendTo(player, "GREEN", "At the moment no plugins were updated!");
			return;
		}
		for (int i=0; i < plugins.size(); i++) {
			th.sendTo(player, "GREEN", plugins.get(i).toString());
		}
	}
}
