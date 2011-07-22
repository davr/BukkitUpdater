package zauberstuhl.BukkitUpdater;

import org.bukkit.entity.Player;
import org.bukkit.event.player.*;

/**
* BukkitUpdater for Bukkit
* @author zauberstuhl
*/

public class BukkitUpdaterPlayerListener extends PlayerListener {
	private final BukkitUpdater plugin;
	public BukkitUpdaterPlayerListener(BukkitUpdater instance) {
		plugin = instance;
	}
	
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.isOp())
			plugin.checkOverview(player);
	}
}