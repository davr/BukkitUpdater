package de.enco.BukkitUpdater.Async.PacketManager;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.enco.BukkitUpdater.ThreadHelper;

public class Install extends Thread {
	protected static final Logger console = Logger.getLogger("Minecraft");
	private Plugin bu;
	private String args;
	private Player player;
	private final ThreadHelper th = new ThreadHelper();
	
	public Install(Plugin bu, String args, Player player) {
		this.bu = bu;
		this.args = args;
		this.player = player;
	}
	
	public void run() {
		try {
			if (th.update(player, args)) {
				th.reloadServer(bu);
				th.sendTo(player, ChatColor.GREEN, "Installed and reloaded plugin "+args);
			} else
				th.sendTo(player, ChatColor.RED, "Installation failed. Is this package available ?!");
			return;
		} catch (IOException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
			return;
		}			
	}
}
