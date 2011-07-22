package zauberstuhl.BukkitUpdater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.command.ColouredConsoleSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
* BukkitUpdater for Bukkit
* @author zauberstuhl
*/

public class BukkitUpdater extends JavaPlugin {
	public String zadb = "";
	public String cwd = System.getProperty("user.dir");
	private final BukkitUpdaterPlayerListener playerListener = new BukkitUpdaterPlayerListener(this);
	private static ColouredConsoleSender console = new ColouredConsoleSender((CraftServer) Bukkit.getServer());
	private final BukkitUpdaterDownloader downloader = new BukkitUpdaterDownloader(this);
	
	@Override
	public void onDisable() {
		console.sendMessage(ChatColor.RED+"[BukkitUpdater] version " + this.getDescription().getVersion() + " disabled.");
	}

	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
		setup();
		console.sendMessage(ChatColor.GREEN+"[BukkitUpdater] version " + this.getDescription().getVersion() + " enabled.");
	}
		
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
		Player player = null;
		String commandName = command.getName().toLowerCase();
		
		if (sender instanceof Player) {
			player = (Player)sender;
		}
		
		if ((commandName.equalsIgnoreCase("u2d")) && (perm(player))) {
			checkOverview(player);
			
			if (args.length == 0) {
				String[] plugins = zadb.split(";");
				
				sendTo(player, "WHITE", "");
				sendTo(player, "WHITE", "BukkitUpdater version "+this.getDescription().getVersion());
				sendTo(player, "WHITE", "This addon was written by zauberstuhl y33");
				if (plugins[0].equalsIgnoreCase(""))
					sendTo(player, "GREEN", "Currently there are no new updates available.");
				else
					sendTo(player, "GOLD", "New Updates are available for:");

				for(int i = 0; plugins.length > i; i++){
					if (plugins[i].matches(".*\\s\\(L\\)")) {
						sendTo(player, "GREEN", plugins[i]);
					} else
						sendTo(player, "RED", plugins[i]);
				}
				return true;
			} else {
				if ((args[0].equalsIgnoreCase("update")) && (!args[1].isEmpty())) {
					try {
						if (downloader.update(args[1])) {
							sendTo(player, "GREEN", "The plugin "+args[1]+" was successfully updated :)");
							sendTo(player, "GREEN", "Please reload the server now via /reload");
							return true;
						} else {
							sendTo(player, "RED", "The plugin "+args[1]+" update failed!");
							sendTo(player, "RED", "You can update only plugins with the (L) behind the name.");
							return true;
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (ProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					helper(player);
					return true;
				}
			}
		}			
		return false;
	}

	public void checkOverview(Player player){
		boolean upd = false;
		String allVersions = "";
		String received = "";
		String buffer;
		Plugin[] plugins = this.getServer().getPluginManager().getPlugins();
		for(int i = 0; i < plugins.length; i++){
			String version = plugins[i].getDescription().getVersion();
			version = version.replaceAll( "[^0-9]", "");
			allVersions += plugins[i].getDescription().getName()+"::"+version+",";
			if(allVersions.length() > 400){
				buffer = sendData(allVersions);
				if(!buffer.equals("false")) {
					received += buffer;
					upd = true;
				}
			}
		}
		buffer = sendData(allVersions);
		if(!buffer.equals("false")) {
			received += buffer;
			upd = true;
		}
		if(upd) {
			sendTo(player, "RED", "New Updates available!! /u2d for details");
		}		
		zadb = received;
	}
	
	public String sendData(String send){
		String token = "";
		String received = null;
		File txt = new File(cwd+"/plugins/BukkitUpdater/token.txt");
		if (txt.exists()) {
			token = readFile(txt);
		}
		try {
			URL adress = new URL( "http://mc.zauberstuhl.de/bukkit_updater/index.pl?s="+send+"&t="+token );
			InputStream in = adress.openStream();
			received = new Scanner( in ).useDelimiter( "\\Z" ).next();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return received;
	}
		
	public void setup(){
		String uuid = UUID.randomUUID().toString();
		File txt = new File(cwd+"/plugins/BukkitUpdater/token.txt");
		File folder = new File(cwd +"/plugins/BukkitUpdater/");
		File backupFolder = new File(cwd +"/plugins/BukkitUpdater/backup/");
		folder.mkdirs();
		backupFolder.mkdirs();

		if (!txt.exists()) {
			writeToFile(txt, uuid);
			console.sendMessage("[BukkitUpdater] Created token:");
			console.sendMessage("[BukkitUpdater] "+uuid);
			String buffer = sendData(uuid);
			if (buffer.equals("success")) {
				console.sendMessage("[BukkitUpdater] Send token success");
			} else
				console.sendMessage("[BukkitUpdater] Ups! Send token failed");
		}
	}
	
	public void helper(Player player) {
			sendTo(player, "RED", "Bukkit Updater Commands:");
			sendTo(player, "WHITE", "");
			sendTo(player, "GOLD", "/u2d");
			sendTo(player, "WHITE", "Shows outdated plugin(s)");
			sendTo(player, "GOLD", "/u2d update <PluginName>");
			sendTo(player, "WHITE", "Update the plugin if there is following tag behind the name \"(L)\"");
			sendTo(player, "GOLD", "/u2d help");
			sendTo(player, "WHITE", "Display this help-text");
	}
	
	public String readFile(File file) {
		String s;
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));
			try {
				s = br.readLine();
				return s;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public void writeToFile(File file, String input) {
		try {
			FileWriter fstream = new FileWriter(file);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(input);
			out.close();
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public static void sendTo(Player player, String color, String string) {
		if (player == null) {
			if (!string.equalsIgnoreCase(""))
				console.sendMessage(ChatColor.valueOf(color)+string);
		} else {
			player.sendMessage(ChatColor.valueOf(color)+string);
		}
	}
	
	public static boolean perm(Player player){
		if (player == null)
			return true;
		else
			return player.isOp();
	}
}
