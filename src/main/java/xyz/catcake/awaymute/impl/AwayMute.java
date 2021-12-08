package xyz.catcake.awaymute.impl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import org.lwjgl.glfw.GLFW;
import xyz.catcake.awaymute.event.TickEventContext;
import xyz.catcake.event.EventSubscribe;

public final class AwayMute {
	private static final int RAMP_DURATION = 60;
	private static final int TRUE = 1;
	private static final float VOLUME_ZERO = 0;
	private static final float VOLUME_MAXIMUM = 1;

	private boolean firstTickOccurred;
	private boolean wasFocused;
	private float originalVolume;
	private long windowHandle;
	private GameOptions options;
	private VolumeRampControl rampControl;

	public AwayMute() {
		wasFocused = true;
		originalVolume = 1;
	}

	private void setLateValues() {
		windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
		options = MinecraftClient.getInstance().options;
		rampControl = new VolumeRampControl(
			VOLUME_ZERO,
			VOLUME_MAXIMUM,
			RAMP_DURATION,
			options
		);
	}

	@SuppressWarnings("unused")
	@EventSubscribe
	public void onTick(final TickEventContext context) {
		if (!firstTickOccurred) {
			setLateValues();
			firstTickOccurred = true;
		}

		rampControl.tick();
		if (!rampControl.ramping()) originalVolume = options.getSoundVolume(SoundCategory.MASTER);

		final var isFocused = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED) == TRUE;

		if (wasFocused && !isFocused) rampControl.rampDown(VOLUME_ZERO);
		else if (!wasFocused && isFocused) rampControl.rampUp(originalVolume);

		wasFocused = isFocused;
	}
}