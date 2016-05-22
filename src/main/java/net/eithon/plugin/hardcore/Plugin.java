package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.event.Listener;

public final class Plugin extends EithonPlugin {
	private Controller _controller;

	@Override
	public void onEnable() {
		super.onEnable();
		Config.load(this);
		this._controller = new Controller(this);
		Listener eventListener = new EventListener(this, this._controller);
		super.activate(eventListener);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		this._controller = null;
	}
}
