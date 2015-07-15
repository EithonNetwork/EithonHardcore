package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlayer;
import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.CommandParser;
import net.eithon.library.plugin.ICommandHandler;
import net.eithon.plugin.hardcore.logic.Controller;

import org.bukkit.command.CommandSender;

public class CommandHandler implements ICommandHandler {
	private static final String BAN_COMMAND = "/ehardcore ban <player> [<hours>]";
	private static final String UNBAN_COMMAND = "/ehardcore unban <player>";
	private static final String LIST_COMMAND = "/ehardcore list";
	
	private Controller _controller;

	public CommandHandler(EithonPlugin eithonPlugin, Controller controller) {
		this._controller = controller;
	}
	
	public boolean onCommand(CommandParser commandParser) {
		String command = commandParser.getArgumentCommand();
		if (command == null) return false;
		
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

		EithonPlayer player = commandParser.getArgumentEithonPlayer(commandParser.getPlayer());
		int hours = commandParser.getArgumentInteger(0);

		hours = this._controller.ban(player, hours);
		Config.M.playerBannedNow.sendMessage(commandParser.getSender(), player.getName(), hours);
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

		EithonPlayer player = commandParser.getArgumentEithonPlayer(commandParser.getPlayer());
		boolean wasReallyUnbanned = this._controller.unban(player);

		if (wasReallyUnbanned) {
			Config.M.playerUnbannedNow.sendMessage(commandParser.getSender(), player.getName());
		} else {
			Config.M.playerIsNotBanned.sendMessage(commandParser.getSender(), player.getName());
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
