package net.eithon.plugin.hardcore.logic;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.IJson;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class BannedPlayer implements Serializable, IJson<BannedPlayer>, IUuidAndName
{
	private static final long serialVersionUID = 1L;
	EithonPlayer _player;
	private LocalDateTime _bannedToTime;

	public BannedPlayer(Player player, int bannedHours) {
		this._player = new EithonPlayer(player);
		this._bannedToTime = LocalDateTime.now().plusHours(bannedHours);
	}

	BannedPlayer() {
	}

	public long getMinutesLeft() {
		return LocalDateTime.now().until(this._bannedToTime, ChronoUnit.MINUTES);
	}

	public String getName() {
		return this._player.getName();
	}

	public UUID getUniqueId() {
		return this._player.getUniqueId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("player", this._player.toJson());
		json.put("bannedToTime", this._bannedToTime.toString());
		return null;
	}
	
	@Override
	public BannedPlayer fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._player = EithonPlayer.getFromJSon(jsonObject.get("player"));
		this._bannedToTime = LocalDateTime.parse((String) jsonObject.get("bannedToTime"));
		return this;
	}

	@Override
	public BannedPlayer factory() {
		return new BannedPlayer();
	}
}
