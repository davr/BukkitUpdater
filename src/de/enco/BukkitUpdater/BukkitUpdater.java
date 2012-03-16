package de.enco.BukkitUpdater;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

//import ru.tehkode.permissions.PermissionManager;
//import ru.tehkode.permissions.bukkit.PermissionsEx;

//import com.nijiko.permissions.PermissionHandler;
//import com.nijikokun.bukkit.Permissions.Permissions;

import de.enco.BukkitUpdater.Async.*;
import de.enco.BukkitUpdater.Async.PacketManager.*;

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

public class BukkitUpdater extends JavaPlugin {
	protected static final Logger console = Logger.getLogger("Minecraft");
	private final ThreadHelper th = new ThreadHelper();
	private final BukkitUpdaterPlayerListener playerListener = new BukkitUpdaterPlayerListener(this);
//	public static PermissionHandler permissionHandler = null;
//	public static PermissionManager permissionExHandler = null;
	
	@Override
	public void onDisable() {
		console.log(Level.INFO, "[BukkitUpdater] version " + this.getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {		
		PluginManager pm = this.getServer().getPluginManager();	
		pm.registerEvents(playerListener, this);
		console.log(Level.INFO, "[BukkitUpdater] version " + this.getDescription().getVersion() + " enabled.");
		try {
			setupBukkitUpdater();
		} catch (IOException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
			onDisable();
		} catch (InvalidConfigurationException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
			onDisable();
		}
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		Player player = null;
		String commandName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		
		if (commandName.equalsIgnoreCase("u2d")) {			
			if (args.length == 0) {
				if (!perm(player, "info", true))
					return false;
				th.sendTo(player, ChatColor.WHITE, "");
				th.sendTo(player, ChatColor.GREEN, "BukkitUpdater version "+this.getDescription().getVersion());
				th.sendTo(player, ChatColor.RED, "Searching plugin informations ...");
				this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
						new Overview(player)
				);
				return true;
			} else {
				if (args[0].equalsIgnoreCase("update") && args.length == 1) {
					if (!perm(player, "update", true))
						return false;
					th.sendTo(player, ChatColor.RED, "Update process triggered manual ...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Updater(this)
					);
					th.sendTo(player, ChatColor.GREEN, "Finished! BukkitUpdater need some time to update all the things in background.");
					th.sendTo(player, ChatColor.GREEN, "The duration depends on the number of your plugins.");
					return true;
				}
				if (args[0].equalsIgnoreCase("search") && args.length > 1) {
					if (!perm(player, "info", true))
						return false;
					th.sendTo(player, ChatColor.GREEN, "Searching in database for '"+args[1]+"' ...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Search(player, args[1])
					);
					return true;
				}
				if (args[0].equalsIgnoreCase("install") && args.length > 1) {
					if (!perm(player, "install", true))
						return false;
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Install(this, args[1], player)
					);
					return true;
				}
				if (args[0].equalsIgnoreCase("ignore") && args.length > 1) {
					if (!perm(player, "ignore", true))
						return false;
					th.sendTo(player, ChatColor.RED, "Searching ignored plugins...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Blacklist(player, args[1])
					);
					return true;
				}
				if (args[0].equalsIgnoreCase("help")) {
					if (!perm(player, "info", true))
						return false;
					th.helper(player);
					return true;
				}
			}
		}			
		return false;
	}
	
	public void setupBukkitUpdater() throws IOException, InvalidConfigurationException {
		th.getConfig = YamlConfiguration.loadConfiguration(th.config);
		th.getExchange = YamlConfiguration.loadConfiguration(th.exchange);
		// create config.yml
		if (!th.config.exists()) {
			th.getConfig.set("autozip", false);
			th.getConfig.set("debug", false);
			th.getConfig.set("plugins.blacklist", Arrays.asList());
			th.getConfig.save(th.config);
		}
		// now create data.yml
		if (!th.exchange.exists()) {
			th.getExchange.set("plugins.unsupported", Arrays.asList());
			th.getExchange.set("plugins.updated", Arrays.asList());
		}
		
		if (!th.backupFolder.exists()) {
			if (!th.backupFolder.mkdir()) {
				console.log(Level.INFO, "[BukkitUpdater][WARN] Creating backup directory failed!");
				onDisable();
			}
		}
		/*
		 * now start the repeater
		 * he will lookup sequentially the other plugin versions
		 * sequenze: 30 minutes 
		 */
//		Integer scheduler = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Updater(this), 60L, 36000L);
//		th.getExchange.set("scheduler.process.id", scheduler);
//		console.log(Level.INFO, "[BukkitUpdater] Now every 30 minutes BukkitUpdater will update automatically your plugins.");
//		// save configuration
		th.getExchange.save(th.exchange);
		
		//setting up permissions
		Plugin permissionsBukkit = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
		
		// PermissionsBukkit
		if (permissionsBukkit != null) {
			console.log(Level.INFO, "[BukkitUpdater] Found and will use plugin "+permissionsBukkit.getDescription().getFullName());
			return;
		}
		console.log(Level.INFO, "[BukkitUpdater] Permission system not detected, defaulting to Op");
	}
	
	public boolean perm(Player player, String perm, Boolean notify){
		// If root
		if (player == null)
			return true;						
	    // SuperPermissions
	    return player.hasPermission("BukkitUpdater."+perm);
	}
}
