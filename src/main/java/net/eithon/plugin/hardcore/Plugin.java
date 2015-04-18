package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class Plugin extends JavaPlugin implements Listener {

	@Override
	public void onEnable() {
		EithonPlugin eithonPlugin = EithonPlugin.get(this);
		eithonPlugin.enable();
		getServer().getPluginManager().registerEvents(new EventListener(eithonPlugin), this);
		Hardcore.get().enable(eithonPlugin);
		Commands.get().enable(this);
	}

	@Override
	public void onDisable() {
		Hardcore.get().disable();
		Commands.get().disable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return Commands.get().onCommand(sender, args);
	}

}
