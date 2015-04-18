package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlayer;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands {
	private static Commands singleton = null;
	private static final String BAN_COMMAND = "/hardcore ban <player> [<hours>]";
	private static final String UNBAN_COMMAND = "/hardcore unban <player>";
	private static final String LIST_COMMAND = "/hardcore list";

	private Commands() {
	}

	static Commands get()
	{
		if (singleton == null) {
			singleton = new Commands();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
	}

	void disable() {
	}

	boolean onCommand(CommandSender sender, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Incomplete command...");
			return false;
		}

		String command = args[0].toLowerCase();
		if (command.equals("ban")) {
			Commands.get().banCommand(sender, args);
		} else if (command.equals("unban")) {
			Commands.get().unbanCommand(sender, args);
		} else if (command.equals("list")) {
			Commands.get().listCommand(sender, args);
		} else {
			sender.sendMessage("Could not understand command.");
			return false;
		}
		return true;
	}

	public void banCommand(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "hardcore.ban")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 2, 3)) {
			sender.sendMessage(BAN_COMMAND);
			return;
		}

		Player player = EithonPlayer.getFromString(args[1]);
		if (player == null) {
			sender.sendMessage(String.format("Unknown player: %s", args[1]));
			return;
		}

		int hours = 0;
		if (args.length > 2) hours = Integer.parseInt(args[2]);

		hours = Hardcore.get().ban(player, hours);
		sender.sendMessage(String.format("Player %s has now been banned from the hardcore world for %d hours.",
				player.getName(), hours));
	}


	public void listCommand(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "hardcore.info")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			sender.sendMessage(LIST_COMMAND);
			return;
		}

		Hardcore.get().list(sender);
	}

	void unbanCommand(CommandSender sender, String[] args)
	{
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "hardcore.unban")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 2, 2)) {
			sender.sendMessage(UNBAN_COMMAND);
			return;
		}

		Player player = EithonPlayer.getFromString(args[1]);
		if (player == null) {
			sender.sendMessage(String.format("Unknown player: %s", args[1]));
			return;
		}

		boolean wasReallyUnbanned = Hardcore.get().unban(player);

		if (wasReallyUnbanned) {
			sender.sendMessage(String.format("Player %s has been unbanned from the hardcore world.", player.getName()));
		} else {
			sender.sendMessage(String.format("Player %s is not banned in the hardcore world.", player.getName()));
		}
	}


	private boolean verifyPermission(Player player, String permission)
	{
		if (player.hasPermission(permission)) return true;
		player.sendMessage("You must have permission " + permission);
		return false;
	}

	private boolean arrayLengthIsWithinInterval(Object[] args, int min, int max) {
		return (args.length >= min) && (args.length <= max);
	}
}
