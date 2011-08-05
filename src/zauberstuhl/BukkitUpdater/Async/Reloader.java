package zauberstuhl.BukkitUpdater.Async;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import zauberstuhl.BukkitUpdater.ThreadHelper;

public class Reloader extends Thread {
	private final ThreadHelper th = new ThreadHelper();
	private Player player;
	private PluginManager pm;
	private Plugin plugin;
	
	public Reloader(Player player, PluginManager pm, Plugin plugin) {
		this.player = player;
		this.pm = pm;
		this.plugin = plugin;
	}
	
	public void run() {
		try {
			pm.disablePlugin(plugin);
			if (!pm.isPluginEnabled(plugin)) {
				pm.enablePlugin(plugin);
				if (pm.isPluginEnabled(plugin)) {
					th.sendTo(player, "GREEN", "["+plugin.getDescription().getName()+"] version "+plugin.getDescription().getVersion() + " enabled.");
					return;
				} else {
					th.sendTo(player, "RED", "["+plugin.getDescription().getName()+"] Failed to re-enable.");
					return;
				}
			} else {
				th.sendTo(player, "RED", "["+plugin.getDescription().getName()+"] Failed to disable.");
				return;
			}
		} catch (NullPointerException e) {
			new Debugger(player, e.getMessage(), "(Something went wrong)");
		}
	}
}
