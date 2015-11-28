package net.eithon.plugin.hardcore.logic;

import java.util.Set;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.hardcore.Config;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Controller {
	private net.eithon.library.json.PlayerCollection<BannedPlayer> _bannedPlayers;
	private EithonPlugin _eithonPlugin = null;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._bannedPlayers = new PlayerCollection<BannedPlayer>(new BannedPlayer());
		delayedLoad(eithonPlugin);
	}

	void disable() {
	}

	public void playerDied(Player player)
	{
		playerDied(new EithonPlayer(player));
	}

	private void playerDied(EithonPlayer player)
	{
		long seconds = ban(player, Config.V.bannedFromWorldSeconds);
		Config.M.bannedUntilMessage.sendMessage(player, TimeMisc.secondsToString(seconds));
		delayedSave();
	}

	public void gotoSpawnArea(Player player) {
		Config.C.spawnCommand.execute();
	}

	public boolean canPlayerTeleport(Player player, Location from, Location to)
	{
		return canPlayerTeleport(new EithonPlayer(player), from, to);
	}

	private boolean canPlayerTeleport(EithonPlayer player, Location from, Location to)
	{
		long minutesLeft = minutesLeftOfBan(player);
		if (minutesLeft <= 0) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s is allowed to teleport", player.getName());
			return true;
		}
		Config.M.stillBanned.sendMessage(player, player.getName(), TimeMisc.minutesToString(minutesLeft, false));
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s is not allowed to teleport", player.getName());
		return false;
	}

	public long ban(EithonPlayer eithonPlayer, long bannedSeconds) {
		this._bannedPlayers.put(eithonPlayer, new BannedPlayer(eithonPlayer, bannedSeconds));
		delayedSave();
		return bannedSeconds;
	}

	public boolean unban(EithonPlayer player) {
		if (!isBanned(player)) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "isBanned(%s) == false", player.getName());
			return false;
		}
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Removing %s from bannedPlayers list.", player.getName());
		this._bannedPlayers.remove(player);
		delayedSave();
		return true;
	}

	public boolean isBanned(Player player) {
		return isBanned(new EithonPlayer(player));
	}

	private boolean isBanned(EithonPlayer player) {
		return minutesLeftOfBan(player) > 0;
	}

	private long minutesLeftOfBan(EithonPlayer player) {
		BannedPlayer bannedPlayer = this._bannedPlayers.get(player);
		if (bannedPlayer == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s is not in bannedPlayers list.", player.getName());
			return 0;
		}
		long minutesLeft = bannedPlayer.getMinutesLeft();
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s has %d minutes left.", player.getName(), minutesLeft);
		if (minutesLeft <= 0) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s is removed from bannedPlayers list.", player.getName());
			this._bannedPlayers.remove(player);
			delayedSave();
			return 0;
		}
		return minutesLeft;
	}
	
	public void list(CommandSender sender) {
		cleanUpBannedPlayers();
		Set<UUID> players = this._bannedPlayers.getPlayers();
		if (players.size() == 0) {
			sender.sendMessage("No players are banned from the hardcore world");
			return;
		}
		for (UUID playerId : players) {
			BannedPlayer bannedPlayer = this._bannedPlayers.get(playerId);
			printPlayerInfo(sender, bannedPlayer);
		}
	}

	private void printPlayerInfo(CommandSender sender, BannedPlayer bannedPlayer)
	{
		long minutesLeft = bannedPlayer.getMinutesLeft();
		if (minutesLeft <= 0) {
			sender.sendMessage(String.format("%s is allowed to teleport to the hardcore world.", bannedPlayer.getName()));
			return;
		}
		Config.M.stillBanned.sendMessage(sender, bannedPlayer.getName(), TimeMisc.minutesToString(minutesLeft, false));
	}

	private void cleanUpBannedPlayers() {
		this._bannedPlayers.entrySet().removeIf(p-> p.getValue().getMinutesLeft() <= 1 );
	}

	private void delayedSave() {
		cleanUpBannedPlayers();	
		this._bannedPlayers.delayedSave(this._eithonPlugin, "banned.json", "BannedPlayers", 0);
	}

	private void delayedLoad(EithonPlugin eithonPlugin) {
		this._bannedPlayers.delayedLoad(eithonPlugin, "banned.json", 0);
		cleanUpBannedPlayers();	
	}
}