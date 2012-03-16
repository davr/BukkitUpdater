package de.enco.BukkitUpdater;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.*;
import org.bukkit.event.Listener;

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

public class BukkitUpdaterPlayerListener implements Listener {
	private final BukkitUpdater plugin;

	public BukkitUpdaterPlayerListener(BukkitUpdater instance) {
		plugin = instance;
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (plugin.perm(player, "info", false)) {
			player.sendMessage(ChatColor.WHITE+"");
			player.sendMessage(ChatColor.GREEN+"BukkitUpdater version "+plugin.getDescription().getVersion()+" is running");
		}
	}
}
