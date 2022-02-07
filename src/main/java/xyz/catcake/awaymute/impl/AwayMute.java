package xyz.catcake.awaymute.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.awaymute.event.TickEndEventContext;
import xyz.catcake.event.EventSubscriber;
import xyz.catcake.log.PrefixedMessageFactory;

import static xyz.catcake.util.BoolUtils.BOOL;

public final class AwayMute {

	private static final float VOLUME_ZERO;
	private static final float VOLUME_MAXIMUM;
	private static final Logger log;

	static {
		VOLUME_ZERO    = 0;
		VOLUME_MAXIMUM = 1;
		log            = LogManager.getLogger(new PrefixedMessageFactory(AwayMuteMod.LOG_PREFIX));
	}

	private final int               rampDuration;
	private       boolean           firstTickOccurred;
	private       boolean           wasFocused;
	/** Retrieved on first tick. */
	private       float             originalVolume;
	/** Retrieved on first tick. */
	private       long              windowHandle;
	/** Retrieved on first tick. */
	private       GameOptions       options;
	/** Retrieved on first tick. */
	private       VolumeRampControl rampControl;

	/** @param rampDuration The duration of the ramp in ticks (Minecraft runs at 20 ticks/sec). */
	public AwayMute(final int rampDuration) {
		this.rampDuration = rampDuration;
		firstTickOccurred = false;
		wasFocused        = true;
		originalVolume    = 1;
	}

	@SuppressWarnings("unused")
	@EventSubscriber
	public void onTick(final TickEndEventContext context) {
		log.atTrace().withLocation().log("#onTick");
		if (!firstTickOccurred) populateLazyFields();

		final boolean isFocused = BOOL.ParseIntStrict(GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED));
		if (wasFocused && !isFocused) {
			originalVolume = options.getSoundVolume(SoundCategory.MASTER);
			log.info(String.format("ramping down to %.2f%% volume", VOLUME_ZERO * 100));
			rampControl.rampDown(VOLUME_ZERO);
		} else if (!wasFocused && isFocused) {
			log.info(String.format("ramping up to %.0f%% volume", originalVolume * 100));
			rampControl.rampUp(originalVolume);
		}

		wasFocused = isFocused;
		rampControl.tick();
	}

	/** These may not be set when this is instantiated, so delay getting them until the first tick. */
	private void populateLazyFields() {
		windowHandle      = MinecraftClient.getInstance().getWindow().getHandle();
		options           = MinecraftClient.getInstance().options;
		rampControl       = new VolumeRampControl(VOLUME_ZERO, VOLUME_MAXIMUM, rampDuration, options);
		firstTickOccurred = true;
	}
}