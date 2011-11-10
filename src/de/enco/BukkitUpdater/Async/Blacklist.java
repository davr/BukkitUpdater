package de.enco.BukkitUpdater.Async;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.enco.BukkitUpdater.ThreadHelper;

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
		File blacklist = new File(bu.getDataFolder(), "blacklist.yml");
		
		try {
			config.load(blacklist);
		} catch (FileNotFoundException e) {
			th.sendTo(player, "GRAY", "(The blacklist was not found)");
		} catch (IOException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		} catch (InvalidConfigurationException e) {
			th.sendTo(player, "GRAY", "(Your blacklist has a wrong configuration)");
		}
		
		List plugins = config.getList("plugins");
		if (input.equalsIgnoreCase("list")) {
			th.sendTo(player, "GRAY", plugins.toString());
			return;
		}

		if (plugins.contains(input)) {
			plugins.remove(input);
			config.set("plugins", plugins);
			th.sendTo(player, "GRAY", plugins.toString());
		} else {
			plugins.add(input);
			config.set("plugins", plugins);
			th.sendTo(player, "GRAY", plugins.toString());
		}
		
		try {
			config.save(blacklist);
		} catch (IOException e) {
			th.sendTo(player, "GRAY", "(Something went wrong)");
		}
	}
}
