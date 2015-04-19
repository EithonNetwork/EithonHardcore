package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.hardcore.logic.Controller;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements ICommandHandler {
	private static final String BAN_COMMAND = "/hardcore ban <player> [<hours>]";
	private static final String UNBAN_COMMAND = "/hardcore unban <player>";
	private static final String LIST_COMMAND = "/hardcore list";
	
	private static ConfigurableMessage playerBannedNowMessage;	
	private static ConfigurableMessage playerUnbannedNowMessage;	
	private static ConfigurableMessage playerIsNotBannedMessage;
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
		Configuration config = eithonPlugin.getConfiguration();
		playerBannedNowMessage = config.getConfigurableMessage("PlayerBannedNowe", 2,
				"Player %s has now been banned from the hardcore world for %d hours.");
		playerUnbannedNowMessage = config.getConfigurableMessage("PlayerUnbannedNow", 1,
				"Player %s has been unbanned from the hardcore world.");
		playerIsNotBannedMessage = config.getConfigurableMessage("PlayerIsNotBanned", 1,
				"Player %s is not banned in the hardcore world.");
	}
	
	public boolean onCommand(CommandParser commandParser) {
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1,1)) return true;

		String command = commandParser.getArgumentStringAsLowercase(0);
		if (command.equals("ban")) {
			banCommand(commandParser);
		} else if (command.equals("unban")) {
			unbanCommand(commandParser);
		} else if (command.equals("list")) {
			listCommand(commandParser);
		} else {
			commandParser.showCommandSyntax();
			return false;
		}
		return true;
	}

	public void banCommand(CommandParser commandParser) {
		if (!commandParser.hasPermissionOrInformSender("hardcore.ban")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 3)) return;

		Player player = commandParser.getArgumentPlayer(1, null);
		int hours = commandParser.getArgumentInteger(2, 0);

		hours = this._controller.ban(player, hours);
		CommandHandler.playerBannedNowMessage.sendMessage(commandParser.getSender(), player.getName(), hours);
	}


	public void listCommand(CommandParser commandParser) {
		if (!commandParser.hasPermissionOrInformSender("hardcore.info")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(1, 1)) return;
		this._controller.list(commandParser.getSender());
	}

	void unbanCommand(CommandParser commandParser)
	{
		if (!commandParser.hasPermissionOrInformSender("hardcore.unban")) return;
		if (!commandParser.hasCorrectNumberOfArgumentsOrShowSyntax(2, 2)) return;

		Player player = commandParser.getArgumentPlayer(1, null);
		boolean wasReallyUnbanned = this._controller.unban(player);

		if (wasReallyUnbanned) {
			CommandHandler.playerUnbannedNowMessage.sendMessage(commandParser.getSender(), player.getName());
		} else {
			CommandHandler.playerIsNotBannedMessage.sendMessage(commandParser.getSender(), player.getName());
		}
	}

	@Override
	public void showCommandSyntax(CommandSender sender, String command) {
		if (command.equals("ban")) {
			sender.sendMessage(BAN_COMMAND);
		} else if (command.equals("unban")) {
			sender.sendMessage(UNBAN_COMMAND);
		} else if (command.equals("list")) {
			sender.sendMessage(LIST_COMMAND);
		} else {
			sender.sendMessage(String.format("Unknown command: %s.", command));
		}
	}
}
