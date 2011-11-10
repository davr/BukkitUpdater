package de.enco.BukkitUpdater;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

//import ru.tehkode.permissions.PermissionManager;
//import ru.tehkode.permissions.bukkit.PermissionsEx;

//import com.nijiko.permissions.PermissionHandler;
//import com.nijikokun.bukkit.Permissions.Permissions;

import de.enco.BukkitUpdater.Async.*;

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

public class BukkitUpdater extends JavaPlugin {
	protected static final Logger console = Logger.getLogger("Minecraft");
	private final ThreadHelper th = new ThreadHelper();
	private final BukkitUpdaterPlayerListener playerListener = new BukkitUpdaterPlayerListener(this);
	//public static PermissionHandler permissionHandler = null;
	//public static PermissionManager permissionExHandler = null;
	
	@Override
	public void onDisable() {
		console.log(Level.INFO, "[BukkitUpdater] version " + this.getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {		
		PluginManager pm = this.getServer().getPluginManager();	
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		setupBukkitUpdater();
		console.log(Level.INFO, "[BukkitUpdater] version " + this.getDescription().getVersion() + " enabled.");
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
				if (!perm(player, "usage", true))
					return false;
				th.sendTo(player, "WHITE", "");
				th.sendTo(player, "WHITE", "BukkitUpdater version "+this.getDescription().getVersion());
				th.sendTo(player, "RED", "Fetching information...");
				this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
						new Overview(player,
								this.getServer().getPluginManager().getPlugins(),
								"u2d"));
				/*
				 * now start the repeater
				 * he will lookup sequentially the other plugin versions
				 * sequenze: 10 minutes 
				 */
				this.getServer().getScheduler().scheduleAsyncRepeatingTask(this,
						new Repeater(this), 20L, 1200L);
				return true;
			} else {
				if ((args[0].equalsIgnoreCase("update")) && args.length > 1) {
					if (!perm(player, "update", true))
						return false;
					th.sendTo(player, "RED", "Updating plugin...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Downloader(player, args[1]));
					return true;
				}
				if (args[0].equalsIgnoreCase("unsupported")) {
					if (!perm(player, "usage", true))
						return false;
					th.sendTo(player, "RED", "Searching unsupported plugins...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Overview(player,
									this.getServer().getPluginManager().getPlugins(),
									"unsupported"));
					return true;
				}
				if (args[0].equalsIgnoreCase("reload") && args.length > 1) {
					if (!perm(player, "reload", true))
						return false;
					PluginManager pm = this.getServer().getPluginManager();
					Plugin reloadPlugin = pm.getPlugin(args[1]);
					
					if (reloadPlugin != null) {
						this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
								new Reloader(player, pm, reloadPlugin));
						return true;
					}
				}
				if (args[0].equalsIgnoreCase("ignore") && args.length > 1) {
					if (!perm(player, "ignore", true))
						return false;
					th.sendTo(player, "RED", "Searching ignored plugins...");
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new Blacklist(this, player, args[1]));
					return true;
				}
				if (args[0].equalsIgnoreCase("help")) {
					th.helper(player);
					return true;
				}
			}
		}			
		return false;
	}
	
	public void setupBukkitUpdater(){
		FileConfiguration config = this.getConfig();
		File blConfigurationFile = new File(getDataFolder(), "blacklist.yml");

		if (!blConfigurationFile.exists()) {
			List<String> blst = Arrays.asList("PluginName1", "PluginName2");
			config.set("plugins", blst);
			try {
				config.save(blConfigurationFile);
			} catch (IOException e) {
				console.log(Level.WARNING, "Was not able to save blacklist.yml: "+e.getMessage());
			}
		}
		
		if (!th.backupFolder.exists())
			if (!th.backupFolder.mkdir()) {
				console.log(Level.INFO, "[BukkitUpdater][WARN] Creating backup directory failed!");
				onDisable();
			}
		
		//setting up permissions
		//Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");
		//Plugin permissionsEx = this.getServer().getPluginManager().getPlugin("PermissionsEx");
		Plugin permissionsBukkit = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
		
		// PermissionsBukkit
		if (permissionsBukkit != null) {
			console.log(Level.INFO, "[BukkitUpdater] Found and will use plugin "+permissionsBukkit.getDescription().getFullName());
			return;
		}
		/*/ Permissions (TheYeti)
		if (permissions != null) {
			String permissionsVersion = permissions.getDescription().getVersion().replaceAll("\\.","");
			// version > 2.5
			if (Integer.parseInt(permissionsVersion) >= 250) {
				permissionHandler = ((Permissions) permissions).getHandler();
				console.log(Level.INFO, "[BukkitUpdater] Found and will use plugin "+((Permissions)permissions).getDescription().getFullName());
				return;
			}
		}
		// PermissionEx
		if (permissionsEx != null) {
			permissionExHandler = PermissionsEx.getPermissionManager();
			console.log(Level.INFO, "[BukkitUpdater] Found and will use plugin "+permissionsEx.getDescription().getFullName());
			return;
		}*/
		console.log(Level.INFO, "[BukkitUpdater] Permission system not detected, defaulting to Op");
	}
	
	public boolean perm(Player player, String perm, Boolean notify){
		// If root
		if (player == null)
			return true;						
	    /*/ TheYeti permission
	    if (permissionHandler != null) {
	    	if (BukkitUpdater.permissionHandler.has(player, "BukkitUpdater."+perm))
	    		return true;
	    	else {
	    		if (notify) th.sendTo(player, "GRAY", "(You have not enough permissions)");
	    		return false;
	    	}
	    }
	    // PermissionEx
	    if (permissionExHandler != null) {
	    	if (BukkitUpdater.permissionExHandler.has(player, "BukkitUpdater."+perm))
		   		return true;
	    	else {
	    		if (notify) th.sendTo(player, "GRAY", "(You have not enough permissions)");
	    		return false;
	    	}
	    }*/
	    // SuperPermissions
	    return player.hasPermission("BukkitUpdater."+perm);
	}
}
