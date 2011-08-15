package zauberstuhl.BukkitUpdater.Async;

import java.io.IOException;

import org.bukkit.entity.Player;

import zauberstuhl.BukkitUpdater.ThreadHelper;

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

public class Debugger extends Thread {
	private String error;
	private Player player;
	private String message;
	private final ThreadHelper th = new ThreadHelper();
	
	public Debugger(Player player, String error, String message) {
		this.error = error;
		this.message = message;
	}

	// starts the debugger and write the output to a log
	public void run() {
		try {
			th.writeToFile(th.debugLog, error);
			th.sendTo(player, "RED", message);
		} catch (IOException e) {
			th.sendTo(player, "RED", "(Something went wrong, also the debug.log is not writable)");
		}
	}
}
