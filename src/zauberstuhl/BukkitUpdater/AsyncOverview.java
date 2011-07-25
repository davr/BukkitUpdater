package zauberstuhl.BukkitUpdater;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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

public class AsyncOverview extends Thread{
	private final BukkitUpdater plugin = new BukkitUpdater();
	private final ThreadHelper th = new ThreadHelper();
	private Player player;
	private Plugin[] plugins;
	private String action;
	
	public String supportedPlugins = "";
	public String unsupportedPlugins = "";
		
	public AsyncOverview(Player player, Plugin[] plugins, String action) {
		this.player = player;
		this.plugins = plugins;
		this.action = action;
	}
	
	public void run() {
		String[] supported;
		String[] unsupported;
		// start a lookup
		boolean u2d = u2d(player);
		
		if (action.equalsIgnoreCase("info")) {
			if (u2d) th.sendTo(player, "RED", "New Updates available!! /u2d for details");
		} else if (action.equalsIgnoreCase("unsupported")) {
			if (unsupportedPlugins.matches(".*;.*")) {
				unsupported = unsupportedPlugins.split(";");
				th.sendTo(player, "WHITE", "Following plugins are not supported by BukkitUpdater:");
				for(int i = 0; unsupported.length > i; i++)
					th.sendTo(player, "RED", unsupported[i]);
			} else
				th.sendTo(player, "GREEN", "All your plugins are supported by BukkitUpdater :)");
		} else {
			if (u2d(player)) {
				supported = supportedPlugins.split(";");
				th.sendTo(player, "GOLD", "New Updates are available for:");
				for(int i = 0; supported.length > i; i++)
					th.sendTo(player, "GREEN", supported[i]);
			} else
				th.sendTo(player, "GREEN", "Currently there are no new updates available.");
			if (unsupportedPlugins.matches(".*;.*")) {
				unsupported = unsupportedPlugins.split(";");
				th.sendTo(player, "RED", "There is/are "+unsupported.length+" unspported plugin(s). For more info: /u2d unsupported");
			}			
		}
	}
	
	public boolean u2d(Player player){
		String allVersions = "";
		String supported = "";
		String unsupported = "";
		String buffer;
		
		for(int i = 0; i < plugins.length; i++){
			String version = plugins[i].getDescription().getVersion();
			version = version.replaceAll( "[^0-9]", "");
			String name = plugins[i].getDescription().getName();
			buffer = name+"::"+version;
			allVersions += name+"::"+version+",";
			buffer = th.sendData(plugin.cwd, buffer);
			
			if(!buffer.equals("false") && !buffer.equals("unsupported"))
				supported += buffer;
			else if (buffer.equals("unsupported"))
				unsupported += name+";";
		}
		supportedPlugins = supported;
		unsupportedPlugins = unsupported;
		
		buffer = th.sendData(plugin.cwd, allVersions);
		if(!buffer.equals("false"))
			return true;
		return false;
	}
}
