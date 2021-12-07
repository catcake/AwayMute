package xyz.catcake.awaymute;

import net.minecraft.sound.SoundCategory;
import xyz.catcake.awaymute.event.MaximizedEventContext;
import xyz.catcake.awaymute.event.MinimizedEventContext;
import xyz.catcake.event.EventSubscribe;

import static xyz.catcake.awaymute.AwayMuteMod.LOG;

public final class AwayMute {
	private float originalVolume;

	public AwayMute() { originalVolume = 1; }

	@SuppressWarnings("unused")
	@EventSubscribe
	public void onMinimize(final MinimizedEventContext context) {
		originalVolume = context.settings().getSoundVolume(SoundCategory.MASTER);
		context.settings().setSoundVolume(SoundCategory.MASTER, 0);
		LOG.info("window unfocused; volume muted");
	}

	@SuppressWarnings("unused")
	@EventSubscribe
	public void onMaximize(final MaximizedEventContext context) {
		context.settings().setSoundVolume(SoundCategory.MASTER, originalVolume);
		LOG.info(String.format("window refocused; volume restored to %.2f", originalVolume));
	}
}