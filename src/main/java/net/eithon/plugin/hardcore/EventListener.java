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
	private static String hardCoreWorldName;
	private EithonPlugin _eithonPlugin;
	private Controller _controller;

	public EventListener(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		this._eithonPlugin = eithonPlugin;
		Configuration config = eithonPlugin.getConfiguration();
		hardCoreWorldName = config.getString("HardcoreWorldName", "");	
	}

	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Enter onPlayerTeleportEvent()");
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Return now if event was cancelled.");
		if (event.isCancelled()) return;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Return now if not in hardcore world.");
		if (!isInHardcoreWorld(event.getTo().getWorld())) return;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Return if player can teleport.");
		boolean canTeleport = this._controller.canPlayerTeleport(event.getPlayer(), event.getFrom(), event.getTo());
		if (canTeleport) return;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "Cancel this teleport event.");
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Enter onPlayerDeathEvent()");
		Player player = event.getEntity();
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Return now if not in hardcore world.");
		if (!isInHardcoreWorld(player.getWorld())) return;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "Ban this player.");
		this._controller.playerDied(player);
	}

	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Enter onPlayerRespawnEvent()");
		Player player = event.getPlayer();
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Return now if not in hardcore world.");
		if (!isInHardcoreWorld(player.getWorld())) return;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Return if player is not banned.");
		boolean isBanned = this._controller.isBanned(event.getPlayer());
		if (!isBanned) return;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Send player to spawn area");
		this._controller.gotoSpawnArea(player);
	}

	private boolean isInHardcoreWorld(World world) {
		if (hardCoreWorldName == null) return false;
		if (hardCoreWorldName.isEmpty()) return false;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "World: \"%s\", hardcore=\"%s\".", world.getName(), hardCoreWorldName);
		return world.getName().equalsIgnoreCase(hardCoreWorldName);
	}
}
