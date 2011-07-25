package zauberstuhl.BukkitUpdater;

import java.io.File;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

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
	private final ThreadHelper th = new ThreadHelper();
	private final BukkitUpdaterPlayerListener playerListener = new BukkitUpdaterPlayerListener(this);
	
	public PermissionHandler permissionHandler;
	public String cwd = System.getProperty("user.dir");
	
	@Override
	public void onDisable() {
		th.console.sendMessage(ChatColor.RED+"[BukkitUpdater] version " + this.getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		setupBukkitUpdater();
		th.console.sendMessage(ChatColor.GREEN+"[BukkitUpdater] version " + this.getDescription().getVersion() + " enabled.");
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		Player player = null;
		String commandName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		
		if ((commandName.equalsIgnoreCase("u2d")) && (perm(player))) {			
			if (args.length == 0) {
				th.sendTo(player, "WHITE", "");
				th.sendTo(player, "WHITE", "BukkitUpdater version "+this.getDescription().getVersion());
				th.sendTo(player, "WHITE", "This addon was written by zauberstuhl y33");
				this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
						new AsyncOverview(player,
								this.getServer().getPluginManager().getPlugins(),
								"u2d"));
				return true;
			} else {
				if ((args[0].equalsIgnoreCase("update")) && (!args[1].isEmpty())) {
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new AsyncDownloader(player, args[1]));
					return true;
				} else if (args[0].equalsIgnoreCase("unsupported")){
					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
							new AsyncOverview(player,
									this.getServer().getPluginManager().getPlugins(),
									"unsupported"));
					return true;
				} else {
					th.helper(player);
					return true;
				}
			}
		}			
		return false;
	}
	
	public void setupBukkitUpdater(){
		String uuid = UUID.randomUUID().toString();
		File txt = new File(cwd+"/plugins/BukkitUpdater/token.txt");
		File folder = new File(cwd +"/plugins/BukkitUpdater/");
		File backupFolder = new File(cwd +"/plugins/BukkitUpdater/backup/");
		
		if (!folder.exists())
			if (!folder.mkdir()) {
				th.console.sendMessage("[BukkitUpdater] Creating main directory failed!");
				onDisable();
			}
		if (!backupFolder.exists())
			if (!backupFolder.mkdir()) {
				th.console.sendMessage("[BukkitUpdater] Creating backup directory failed!");
				onDisable();
			}
						
		if (!txt.exists()) {
			th.writeToFile(txt, uuid);
			th.console.sendMessage("[BukkitUpdater] Created token:");
			th.console.sendMessage("[BukkitUpdater] "+uuid);
			String buffer = th.sendData(cwd, uuid);
			if (buffer.equals("success")) {
				th.console.sendMessage("[BukkitUpdater] Send token success");
			} else
				th.console.sendMessage("[BukkitUpdater] Ups! Send token failed");
		}
		
		//setting up permissions
		if (permissionHandler != null)
			return;
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (permissionsPlugin == null) {
	        th.console.sendMessage("[BukkitUpdater] Permission system not detected, defaulting to OP");
	        return;
	    }
	    permissionHandler = ((Permissions) permissionsPlugin).getHandler();
	    th.console.sendMessage("[BukkitUpdater] Found and will use plugin "+((Permissions)permissionsPlugin).getDescription().getFullName());
	}
	
	public boolean perm(Player player){
		if (player == null)
			return true;
		
		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
	    if (permissionsPlugin == null)
	    	return player.isOp();
	    else {
	    	if (this.permissionHandler.has(player, "BukkitUpdater.usage"))
	    		return true;
	    	else
	    		return false;
	    }
	}
}
