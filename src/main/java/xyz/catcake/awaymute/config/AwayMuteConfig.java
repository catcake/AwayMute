package xyz.catcake.awaymute.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.log.PrefixedMessageFactory;

import static xyz.catcake.awaymute.AwayMuteMod.LOG_PREFIX;

@Config(name = AwayMuteMod.MOD_ID)
public final class AwayMuteConfig implements ConfigData {

	@ConfigEntry.Gui.Excluded private static final int    volumeRampRateDefault;
	@ConfigEntry.Gui.Excluded private static final Logger log;

	static {
		volumeRampRateDefault = 20;
		log                   = LogManager.getLogger(new PrefixedMessageFactory(LOG_PREFIX));
	}

	@ConfigEntry.BoundedDiscrete(min = 0, max = 100)
	private int volumeRampRate;

	public AwayMuteConfig() { volumeRampRate = volumeRampRateDefault; }

	public int volumeRampRate() { return volumeRampRate; }

	@Override
	public void validatePostLoad() {
		if (volumeRampRate >= 0 && volumeRampRate <= 100) return;
		log.warn("volumeRampRate out of bounds ({}), set to {}", volumeRampRate, volumeRampRateDefault);
		volumeRampRate = volumeRampRateDefault;
	}
}