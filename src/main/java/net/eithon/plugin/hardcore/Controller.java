package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.PluginMisc;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.bungee.EithonBungeePlugin;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class Controller {
	private EithonPlugin _eithonPlugin = null;
	private EithonBungeePlugin _eithonBungeePlugin;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		connectToEithonBungee(eithonPlugin);
	}

	private void connectToEithonBungee(EithonPlugin eithonPlugin) {
		Plugin plugin = PluginMisc.getPlugin("EithonBungee");
		if (plugin != null 
				&& plugin.isEnabled()
				&& (plugin instanceof EithonPlugin)) {
			this._eithonBungeePlugin = (EithonBungeePlugin) plugin;
			eithonPlugin.getEithonLogger().info("Succesfully hooked into the EithonBungee plugin!");
		} else {
			this._eithonBungeePlugin = null;
			eithonPlugin.getEithonLogger().warning("EithonHardcore works better with the EithonBungee plugin");			
		}
	}

	public void playerDied(Player player)
	{
		long seconds = ban(player, Config.V.bannedFromServerSeconds);
		if (seconds == 0) return;
		Config.M.bannedUntilMessage.sendMessage(player, TimeMisc.secondsToString(seconds));
	}

	public long ban(Player player, long seconds) {
		if (this._eithonBungeePlugin == null) {
			this._eithonPlugin.getEithonLogger().warning("EithonHardcore can't ban players without the EithonBungee plugin");
			return 0;
		} 
		this._eithonPlugin.getEithonLogger().info("Banning player %s, %s", player, TimeMisc.secondsToString(seconds));
		this._eithonBungeePlugin.getApi().banPlayerOnThisServer(player, seconds);
		return seconds;
	}
}