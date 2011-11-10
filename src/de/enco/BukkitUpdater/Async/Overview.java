package de.enco.BukkitUpdater.Async;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.enco.BukkitUpdater.ThreadHelper;

public class Overview extends Thread {
	private final ThreadHelper th = new ThreadHelper();
	protected static final Logger console = Logger.getLogger("Minecraft");
	private Player player;
	private Plugin bu;
	
	public Overview(Player player, Plugin bu) {
		this.player = player;
		this.bu = bu;
	}
	
	public void run() {
		FileConfiguration config = bu.getConfig();
		try {
			config.load(th.config);
		} catch (FileNotFoundException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (IOException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		} catch (InvalidConfigurationException e) {
			console.log(Level.WARNING, "[BukkitUpdater] Something went wrong: "+e.getMessage());
		}
		List plugins = config.getList("plugins.updated");
		if (plugins.isEmpty()) {
			th.sendTo(player, "GREEN", "At the moment no plugins were updated!");
			return;
		}
		for (int i=0; i < plugins.size(); i++) {
			th.sendTo(player, "GREEN", config.getName());
		}
	}
}
