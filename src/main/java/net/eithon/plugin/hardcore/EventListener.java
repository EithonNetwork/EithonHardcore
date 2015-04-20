package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.Configuration;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.plugin.hardcore.logic.Controller;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public final class EventListener implements Listener {
	private EithonPlugin _eithonPlugin;
	private Controller _controller;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
	}

	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		debug("onPlayerTeleportEvent", "Enter");
		if (event.isCancelled()) {
			debug("onPlayerTeleportEvent", "Event was already cancelled. Return.");
			return;
		}
		if (!isInHardcoreWorld(event.getTo().getWorld())) {
			debug("onPlayerTeleportEvent", "Not in a hardcore world. Return.");
			return;
		}
		boolean canTeleport = this._controller.canPlayerTeleport(event.getPlayer(), event.getFrom(), event.getTo());
		if (canTeleport) {
			debug("onPlayerTeleportEvent", "Player can teleport. Return.");
			return;
		}
		debug("onPlayerTeleportEvent", "Cancel this teleport event. Return.");
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		debug("onPlayerDeathEvent", "Enter");
		Player player = event.getEntity();
		if (!isInHardcoreWorld(player.getWorld())) {
			debug("onPlayerDeathEvent", "Not in a hardcore world. Return.");
			return;
		}
		debug("onPlayerDeathEvent", "Ban this player. Return.");
		this._controller.playerDied(player);
	}

	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		debug("onPlayerRespawnEvent", "Enter");
		Player player = event.getPlayer();
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Return now if not in hardcore world.");
		if (!isInHardcoreWorld(player.getWorld())) {
			debug("onPlayerRespawnEvent", "Not in a hardcore world. Return.");
			return;
		}
		boolean isBanned = this._controller.isBanned(event.getPlayer());
		if (!isBanned) {
			debug("onPlayerRespawnEvent", "Player is still banned. Return.");
			return;
		}
		debug("onPlayerRespawnEvent", "Send player to spawn area. Return.");
		this._controller.gotoSpawnArea(player);
	}

	private boolean isInHardcoreWorld(World world) {
		if (Config.V.hardCoreWorldName == null) return false;
		if (Config.V.hardCoreWorldName.isEmpty()) return false;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "World = \"%s\", hardcore=\"%s\".", 
				world.getName(), Config.V.hardCoreWorldName);
		return world.getName().equalsIgnoreCase(Config.V.hardCoreWorldName);
	}

	private void debug(String method, String message) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.VERBOSE, "%s: %s", method, message);
	}
}
