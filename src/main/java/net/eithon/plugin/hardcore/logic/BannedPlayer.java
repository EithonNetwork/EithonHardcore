package net.eithon.plugin.hardcore.logic;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.json.JsonObject;

import org.json.simple.JSONObject;

public class BannedPlayer extends JsonObject<BannedPlayer> implements Serializable, IUuidAndName
{
	private static final long serialVersionUID = 1L;
	EithonPlayer _player;
	private LocalDateTime _bannedToTime;

	public BannedPlayer(EithonPlayer eithonPlayer, long bannedSeconds) {
		this._player = eithonPlayer;
		this._bannedToTime = LocalDateTime.now().plusSeconds(bannedSeconds);
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
		return json;
	}
	
	@Override
	public JSONObject toJsonObject() {
		return (JSONObject) toJson();
	}
	
	@Override
	public String toJsonString() {
		return toJsonObject().toJSONString();
	}

	@Override
	public BannedPlayer fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._player = EithonPlayer.getFromJson(jsonObject.get("player"));
		this._bannedToTime = LocalDateTime.parse((String) jsonObject.get("bannedToTime"));
		return this;
	}

	@Override
	public BannedPlayer factory() {
		return new BannedPlayer();
	}
}
