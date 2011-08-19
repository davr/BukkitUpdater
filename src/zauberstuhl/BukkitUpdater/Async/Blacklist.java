package zauberstuhl.BukkitUpdater.Async;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;

import zauberstuhl.BukkitUpdater.ThreadHelper;

public class Blacklist extends Thread {
	private final ThreadHelper th = new ThreadHelper();
	private Player player;
	private String input;

	public Blacklist(Player player, String input) {
		this.player = player;
		this.input = input;
	}
	
	public void run() {
		// are there multiple plugins?
		input = input.replaceAll(",", ",\n");
		
		try {
			String blacklist = th.readFile(th.blacklist);
			Pattern pattern = Pattern.compile("(?i).*"+input+".*", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(blacklist);			
			
			if (input.equalsIgnoreCase("list")) {
				blacklist = blacklist.replaceAll("#.*?\\n", "");
				blacklist = blacklist.replaceAll("\\n", "");
				th.sendTo(player, "GRAY", blacklist);
			} else {
				if (matcher.find()) {
					blacklist = blacklist.replaceAll(input+",\n", "");
					th.writeToFile(th.blacklist, blacklist);
					th.sendTo(player, "GRAY", "(The plugin(s) "+input+" was/were deleted from blacklist.txt)");
				} else {
					th.writeToFile(th.blacklist, blacklist+input+",\n");
					th.sendTo(player, "GRAY", "(The plugin(s) "+input+" was/were written to blacklist.txt)");
				}
			}			
		} catch (IOException e) {
			new Debugger(player, e.getMessage(), "(Something went wrong)");
		}
	}
}
