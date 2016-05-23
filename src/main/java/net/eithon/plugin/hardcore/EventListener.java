package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Logger.DebugPrintLevel;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class EventListener implements Listener {
	private EithonPlugin _eithonPlugin;
	private Controller _controller;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		debug("onPlayerDeathEvent", "Enter");
		Player player = event.getEntity();
		debug("onPlayerDeathEvent", "Ban this player. Return.");
		this._controller.playerDied(player);
	}

	private void debug(String method, String message) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}
