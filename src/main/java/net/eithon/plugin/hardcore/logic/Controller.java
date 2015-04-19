package net.eithon.plugin.hardcore.logic;

import java.io.File;
import java.util.Set;
import java.util.UUID;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.json.Converter;
import net.eithon.library.json.PlayerCollection;
import net.eithon.library.plugin.Logger.DebugPrintLevel;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.json.simple.JSONObject;

public class Controller {
	private static ConfigurableMessage bannedUntilMessage;
	private static ConfigurableMessage stillBannedHoursMessage;
	private static ConfigurableMessage stillBannedMinutesMessage;
	private static ConfigurableCommand spawnCommand;
	private net.eithon.library.json.PlayerCollection<BannedPlayer> _bannedPlayers;

	private int _bannedFromWorldHours;

	private EithonPlugin _eithonPlugin = null;

	public Controller(EithonPlugin eithonPlugin){
		Configuration config = eithonPlugin.getConfiguration();
		this._eithonPlugin = eithonPlugin;
		this._bannedFromWorldHours = config.getInt("BannedFromWorldHours", 72);
		Controller.spawnCommand = config.getConfigurableCommand("commands.TeleportToSpawn", 1,"/spawn");
		Controller.bannedUntilMessage = config.getConfigurableMessage("messages.BannedUntil", 1,
				"Due to dying in the hardcore world, you have now been banned from this world for %d hours.");
		Controller.stillBannedHoursMessage = config.getConfigurableMessage("StillBannedMinutesMessage", 2,
				"Due to your earlier death in the hardcore world, you are banned for another %d hours and %d minutes.");
		Controller.stillBannedMinutesMessage = config.getConfigurableMessage("StillBannedMinutesMessage", 1,
				"Due to your earlier death in the hardcore world, you are banned for another %d minutes more.");
		this._bannedPlayers = new PlayerCollection<BannedPlayer>(new BannedPlayer());
		delayedLoad();
	}

	void disable() {
	}

	public void playerDied(Player player)
	{
		int hours = ban(player);
		Controller.bannedUntilMessage.sendMessage(player, hours);
		delayedSave();
	}
	
	public void gotoSpawnArea(Player player) {
		Controller.spawnCommand.execute();
	}

	public boolean canPlayerTeleport(Player player, Location from, Location to)
	{
		long minutesLeft = minutesLeftOfBan(player);
		if (minutesLeft <= 0) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s is allowed to teleport", player.getName());
			return true;
		}
		if (minutesLeft < 120) {
			Controller.stillBannedMinutesMessage.sendMessage(player, minutesLeft);
		} else {
			long hoursLeft = minutesLeft/60;
			long restMinutes = minutesLeft - hoursLeft*60;
			Controller.stillBannedHoursMessage.sendMessage(player, hoursLeft, restMinutes);
		}
		this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MINOR, "%s is not allowed to teleport", player.getName());
		return false;
	}

	public int ban(Player player) {
		return ban(player, 0);
	}

	public int ban(Player player, int bannedHours) {
		if (bannedHours <= 0) bannedHours = this._bannedFromWorldHours;
		this._bannedPlayers.put(player, new BannedPlayer(player, bannedHours));
		delayedSave();
		return bannedHours;
	}

	public boolean unban(Player player) {
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
		return minutesLeftOfBan(player) > 0;
	}

	private long minutesLeftOfBan(Player player) {
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
		File jsonFile = new File(this._eithonPlugin.getDataFolder(), "banned.json");
		JSONObject json = Converter.fromBody("bannedPlayers", 1, (Object) this._bannedPlayers.toJson());
		Converter.save(jsonFile, json);
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
		JSONObject data = Converter.load(this._eithonPlugin, file);
		if (data == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The file was empty.");
			return;			
		}
		JSONObject payload = (JSONObject)Converter.toBodyPayload(data);
		if (payload == null) {
			this._eithonPlugin.getEithonLogger().debug(DebugPrintLevel.MAJOR, "The banned players payload was empty.");
			return;
		}
		this._bannedPlayers.fromJson(payload);
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
		if (minutesLeft < 120) {
			Controller.stillBannedMinutesMessage.sendMessage(sender, minutesLeft);
		} else {
			long hoursLeft = minutesLeft/60;
			long restMinutes = minutesLeft - hoursLeft*60;
			Controller.stillBannedHoursMessage.sendMessage(sender, hoursLeft, restMinutes);
		}
	}


	private void cleanUpBannedPlayers() {
		Set<UUID> players = this._bannedPlayers.getPlayers();
		for (UUID playerId : players) {
			BannedPlayer bannedPlayer = this._bannedPlayers.get(playerId);
			if (bannedPlayer.getMinutesLeft() <= 1) this._bannedPlayers.remove(playerId);
		}
	}
}