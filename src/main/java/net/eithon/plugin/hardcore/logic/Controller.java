package net.eithon.plugin.hardcore.logic;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.FileContent;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.time.TimeMisc;
import net.eithon.plugin.hardcore.Config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class Controller {
	private net.eithon.library.json.PlayerCollection<BannedPlayer> _bannedPlayers;
	private EithonPlugin _eithonPlugin = null;

	public Controller(EithonPlugin eithonPlugin){
		this._eithonPlugin = eithonPlugin;
		this._bannedPlayers = new PlayerCollection<BannedPlayer>(new BannedPlayer());
		delayedLoad();
	}

	void disable() {
	}

	public void playerDied(Player player)
	{
		playerDied(new EithonPlayer(player));
	}

	private void playerDied(EithonPlayer player)
	{
		int hours = ban(player);
		Config.M.bannedUntilMessage.sendMessage(player, hours);
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
		Config.M.stillBanned.sendMessage(player, player.getName(), TimeMisc.minutesToString(minutesLeft, true));
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s is not allowed to teleport", player.getName());
		return false;
	}

	public int ban(EithonPlayer player) {
		return ban(player, 0);
	}

	public int ban(EithonPlayer eithonPlayer, int bannedHours) {
		if (bannedHours <= 0) bannedHours = Config.V._bannedFromWorldHours;
		this._bannedPlayers.put(eithonPlayer, new BannedPlayer(eithonPlayer, bannedHours));
		delayedSave();
		return bannedHours;
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

	private void delayedSave() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				saveNow();
			}
		});
	}

	void saveNow()
	{
		cleanUpBannedPlayers();

		if (this._bannedPlayers == null) return;
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "Saving %d banned players.", this._bannedPlayers.size());
		File jsonFile = new File(this._eithonPlugin.getDataFolder(), "banned.json");
		FileContent fileContent = new FileContent("bannedPlayers", 1, this._bannedPlayers.toJson());
		fileContent.save(jsonFile);
	}

	private void delayedLoad() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this._eithonPlugin, new Runnable() {
			public void run() {
				loadJson();
			}
		}, 200L);
	}

	void loadJson() {
		File file = new File(this._eithonPlugin.getDataFolder(), "banned.json");
		FileContent fileContent = FileContent.loadFromFile(file);
		if (fileContent == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The file was empty.");
			return;			
		}
		this._bannedPlayers.fromJson(fileContent.getPayload());
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
		Set<UUID> players = this._bannedPlayers.getPlayers();
		for (UUID playerId : players) {
			BannedPlayer bannedPlayer = this._bannedPlayers.get(playerId);
			if (bannedPlayer.getMinutesLeft() <= 1) this._bannedPlayers.remove(playerId);
		}
	}
}