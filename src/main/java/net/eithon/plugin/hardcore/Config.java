package net.eithon.plugin.hardcore;

import java.util.List;

import net.eithon.library.extensions.EithonPlugin;
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

		static void load(Configuration config) {
			hardCoreWorldName = config.getString("HardcoreWorldName", "");	
		}

	}
	public static class C {

		static void load(Configuration config) {
		}

	}
	public static class M {
		public static ConfigurableMessage playerBannedNow;	
		public static ConfigurableMessage playerUnbannedNow;	
		public static ConfigurableMessage playerIsNotBanned;

		static void load(Configuration config) {
			playerBannedNow = config.getConfigurableMessage("PlayerBannedNowe", 2,
					"Player %s has now been banned from the hardcore world for %d hours.");
			playerUnbannedNow = config.getConfigurableMessage("PlayerUnbannedNow", 1,
					"Player %s has been unbanned from the hardcore world.");
			playerIsNotBanned = config.getConfigurableMessage("PlayerIsNotBanned", 1,
					"Player %s is not banned in the hardcore world.");
		}		
	}

}
