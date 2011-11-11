package de.enco.BukkitUpdater.Async;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
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

public class Blacklist extends Thread {
	private final ThreadHelper th = new ThreadHelper();
	private Plugin bu;
	private Player player;
	private String input;

	public Blacklist(Plugin bu, Player player, String input) {
		this.bu = bu;
		this.player = player;
		this.input = input;
	}
	
	public void run() {
		FileConfiguration config = bu.getConfig();

		try {
			config.load(th.config);
		} catch (FileNotFoundException e) {
			th.sendTo(player, "GRAY", "(The blacklist was not found)");
		} catch (IOException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		} catch (InvalidConfigurationException e) {
			th.sendTo(player, "GRAY", "(Your blacklist has a wrong configuration)");
		}
		List plugins = config.getList("plugins.blacklist");
		if (input.equalsIgnoreCase("list")) {
			th.sendTo(player, "GRAY", plugins.toString());
			return;
		}
		if (plugins.contains(input)) {
			plugins.remove(input);
			config.set("plugins.blacklist", plugins);
			th.sendTo(player, "GRAY", plugins.toString());
		} else {
			plugins.add(input);
			config.set("plugins.blacklist", plugins);
			th.sendTo(player, "GRAY", plugins.toString());
		}
		try {
			config.save(th.config);
		} catch (IOException e) {
			th.sendTo(player, "GRAY", "(Cannot save blacklist.yml)");
		}
	}
}
