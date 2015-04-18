package net.eithon.plugin.hardcore;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import net.eithon.library.core.IUuidAndName;
import net.eithon.library.json.Converter;
import net.eithon.library.json.IJson;

import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class BannedPlayer implements Serializable, IJson<BannedPlayer>, IUuidAndName
{
	private static final long serialVersionUID = 1L;
	private UUID _id;
	private String _name;
	private LocalDateTime _bannedToTime;

	public BannedPlayer(Player player, int bannedHours) {
		this._id = player.getUniqueId();
		this._name = player.getName();
		this._bannedToTime = LocalDateTime.now().plusHours(bannedHours);
	}

	BannedPlayer() {
	}

	public long getMinutesLeft() {
		return LocalDateTime.now().until(this._bannedToTime, ChronoUnit.MINUTES);
	}

	public String getName() {
		return this._name;
	}

	public UUID getUniqueId() {
		return this._id;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object toJson() {
		JSONObject json = new JSONObject();
		json.put("player", Converter.fromPlayer(this._id, this._name));
		json.put("bannedToTime", this._bannedToTime.toString());
		return null;
	}
	
	@Override
	public void fromJson(Object json) {
		JSONObject jsonObject = (JSONObject) json;
		this._id = Converter.toPlayerId((JSONObject) jsonObject.get("player"));
		this._name = Converter.toPlayerName((JSONObject) jsonObject.get("player"));
		this._bannedToTime = LocalDateTime.parse((String) jsonObject.get("bannedToTime"));
	}

	@Override
	public BannedPlayer factory() {
		return new BannedPlayer();
	}
}
