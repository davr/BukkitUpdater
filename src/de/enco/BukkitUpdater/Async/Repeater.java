package de.enco.BukkitUpdater.Async;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

import com.esotericsoftware.wildcard.Paths;

import de.enco.BukkitUpdater.ThreadHelper;

public class Repeater extends Thread{
	protected static final Logger console = Logger.getLogger("Minecraft");
	private final ThreadHelper th = new ThreadHelper();
	private Plugin bu;
	private ArrayList<Plugin> supported = new ArrayList<Plugin>();
	private ArrayList<Plugin> needUpdate = new ArrayList<Plugin>();
	private ArrayList<Plugin> unsupported = new ArrayList<Plugin>();
	
	public Repeater(Plugin bu) {
		this.bu = bu;
	}
	
	public void run() {
		//debug
		console.log(Level.WARNING, "[DEBUG] STARTING UPDATE MODE");
		
		PluginManager pm = bu.getServer().getPluginManager();
		Plugin[] plugins = pm.getPlugins();
		try {
			if (lookup(plugins)) {
				//debug
				console.log(Level.WARNING, "[DEBUG] supported: "+supported.toString());
				console.log(Level.WARNING, "[DEBUG] needUpdate: "+needUpdate.toString());
				console.log(Level.WARNING, "[DEBUG] unsupported: "+unsupported.toString());
				
				for (int i=0; i < needUpdate.size(); i++) {
					String plugin = needUpdate.get(i).getDescription().getName();
					console.log(Level.WARNING, "[DEBUG] update: "+plugin);
					if (update(plugin)) {
						bu.getServer().broadcastMessage(ChatColor.GREEN+"The plugin "+plugin+" was successfully updated.");
						if (reload(bu, pm, pm.getPlugin(plugin)))
							bu.getServer().broadcastMessage(ChatColor.GREEN+"The plugin "+plugin+" was successfully reloaded.");
						else
							bu.getServer().broadcastMessage(ChatColor.RED+"Please reload the server manually!");
					} else {
						console.log(Level.WARNING, "The plugin "+plugin+" update failed!");
					}
				}
			} else
				console.log(Level.WARNING, "(3nc0.de not reachable)");
		} catch (IllegalStateException e) {
			console.log(Level.WARNING, "[DEBUG] "+e.getMessage());
		} catch (MalformedURLException e) {
			console.log(Level.WARNING, "[DEBUG] "+e.getMessage());
		} catch (ProtocolException e) {
			console.log(Level.WARNING, "[DEBUG] "+e.getMessage());
		} catch (IOException e) {
			console.log(Level.WARNING, "[DEBUG] "+e.getMessage());
		}
	}
	
	public boolean update(String pluginName) throws IllegalStateException, MalformedURLException, ProtocolException, IOException {
		OutputStream os;
		String url = th.sendData(pluginName+":url");
		
		console.log(Level.WARNING, "[DEBUG] link = "+pluginName+":"+url);
		
		if (url.equalsIgnoreCase("false") || url.equalsIgnoreCase("")) {
			return false;
		}

		backup(pluginName);
		
		os = new FileOutputStream(th.cwd+"/plugins/"+pluginName+".jar");
		downloadFile(url, os);
		return true;
	}
		
	public static void downloadFile(String url_str, OutputStream os)
	throws IllegalStateException, MalformedURLException,
	ProtocolException, IOException {
		URL url = new URL(url_str.replace(" ", "%20"));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
        	byte tmp_buffer[] = new byte[4096];
            InputStream is = conn.getInputStream();
            int n;
            while ((n = is.read(tmp_buffer)) > 0) {
            	os.write(tmp_buffer, 0, n);
            	os.flush();
            }
        } else {
        	throw new IllegalStateException("HTTP response: " + responseCode);
        }
	}
	
	public void backup(String plugin) throws IOException{
		Paths paths = new Paths();
		paths.regex(th.cwd+"/plugins", ".*?"+plugin+".*?\\.jar");
		
		for (int i=0; i < paths.getPaths().size(); i++) {
			InputStream in = new FileInputStream(
					new File(paths.getPaths().get(i)));
			OutputStream out = new FileOutputStream(
					new File(th.cwd+"/plugins/BukkitUpdater/backup/"+plugin+".jar.backup"));
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
		}
		// delete old versions
		paths.delete();
	}
	
	public boolean lookup(Plugin[] plugins) throws IOException{
		String data = "";
		String response;
		
		// we have to clear the list first
		supported.clear();
		needUpdate.clear();
		unsupported.clear();
		for(int i = 0; i < plugins.length; i++){
			String version = plugins[i].getDescription().getVersion();
			String name = plugins[i].getDescription().getName();
			data += name+":"+version+"::";
		}
		response = th.sendData(data);

		String []result = response.split(":");
		for(int i = 0; i < result.length; i++) {
			int x = Integer.parseInt(result[i]);
			if (x == 0) {
				supported.add(plugins[i]);
				needUpdate.add(plugins[i]);
			} else if (x == 1) {
				supported.add(plugins[i]);
			} else if (x == 2) {
				unsupported.add(plugins[i]);
			}
		}
		// are there updates?
		if(!response.matches("1"))
			return true;
		return false;
	}
	
	public static boolean reload(Plugin bu, PluginManager pm, Plugin plugin) {
		//if (plugin.getDescription().getName().equalsIgnoreCase("BukkitUpdater")) {
		//	console.log(Level.WARNING, "BukkitUpdater cannot restart on his own. Please reload the server ...");
		//	return false;
		//}
		// now try to reload
		try {
			pm.disablePlugin(plugin);
			if (!pm.isPluginEnabled(plugin)) {
				pm.enablePlugin(plugin);
				if (pm.isPluginEnabled(plugin))
					return true;
				else
					return false;
			} else
				return false;
		} catch (NullPointerException e) {
			console.log(Level.WARNING, "[DEBUG] "+e.getMessage());
			return false;
		}
	}
}
