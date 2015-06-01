package net.eithon.plugin.hardcore;

import net.eithon.library.extensions.EithonPlugin;
import net.eithon.library.plugin.ConfigurableCommand;
import net.eithon.library.plugin.ConfigurableMessage;
import net.eithon.library.plugin.Configuration;

public class Config {
	public static void load(EithonPlugin plugin)
	{
		Configuration config = plugin.getConfiguration();
		V.load(config);
		C.load(config);
		M.load(config);

	}
	public static class V {
		public static String hardCoreWorldName;
		public static int _bannedFromWorldHours;

		static void load(Configuration config) {
			hardCoreWorldName = config.getString("HardcoreWorldName", "");	
			_bannedFromWorldHours = config.getInt("BannedFromWorldHours", 72);
		}

	}
	public static class C {
		public static ConfigurableCommand spawnCommand;

		static void load(Configuration config) {
			spawnCommand = config.getConfigurableCommand("commands.TeleportToSpawn", 1,"/spawn");
		}

	}
	public static class M {
		public static ConfigurableMessage playerBannedNow;	
		public static ConfigurableMessage playerUnbannedNow;	
		public static ConfigurableMessage playerIsNotBanned;
		public static ConfigurableMessage bannedUntilMessage;
		public static ConfigurableMessage stillBannedHoursMessage;
		public static ConfigurableMessage stillBannedMinutesMessage;

		static void load(Configuration config) {
			playerBannedNow = config.getConfigurableMessage("PlayerBannedNowe", 2,
					"Player %s has now been banned from the hardcore world for %d hours.");
			playerUnbannedNow = config.getConfigurableMessage("PlayerUnbannedNow", 1,
					"Player %s has been unbanned from the hardcore world.");
			playerIsNotBanned = config.getConfigurableMessage("PlayerIsNotBanned", 1,
					"Player %s is not banned in the hardcore world.");
			bannedUntilMessage = config.getConfigurableMessage("messages.BannedUntil", 1,
					"Due to dying in the hardcore world, you have now been banned from this world for %d hours.");
			stillBannedHoursMessage = config.getConfigurableMessage("messages.StillBannedHours", 2,
					"Due to your earlier death in the hardcore world, you are banned for another %d hours and %d minutes.");
			stillBannedMinutesMessage = config.getConfigurableMessage("messages.StillBannedMinutes", 1,
					"Due to your earlier death in the hardcore world, you are banned for another %d minutes more.");
		}		
	}

}
