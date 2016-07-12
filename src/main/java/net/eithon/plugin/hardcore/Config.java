package net.eithon.plugin.hardcore;

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
		public static long bannedFromServerSeconds;

		static void load(Configuration config) {
			bannedFromServerSeconds = config.getSeconds("BannedFromServerTimeSpan", "72h");
		}

	}
	public static class C {
		static void load(Configuration config) {
		}

	}
	public static class M {
		public static ConfigurableMessage bannedUntilMessage;

		static void load(Configuration config) {
			bannedUntilMessage = config.getConfigurableMessage("messages.BannedUntil", 1,
					"Due to dying in the hardcore world, you have now been banned from this world for %s.");
		}		
	}

}
