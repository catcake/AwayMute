package xyz.catcake.awaymute;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import org.lwjgl.glfw.GLFW;
import xyz.catcake.awaymute.event.TickEventContext;
import xyz.catcake.event.EventSubscribe;

import static xyz.catcake.awaymute.AwayMuteMod.LOG;

public final class AwayMute {
	private static final int TRUE = 1;
	private static final float VOLUME_ZERO = 0;

	private boolean firstTickOccurred;
	private boolean wasFocused;
	private float originalVolume;
	private long windowHandle;
	private GameOptions options;

	public AwayMute() {
		wasFocused = true;
		originalVolume = 1;
	}

	private void setLateValues() {
		windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
		options = MinecraftClient.getInstance().options;
	}

	@SuppressWarnings("unused")
	@EventSubscribe
	public void onTick(final TickEventContext context) {
		if (!firstTickOccurred) {
			setLateValues();
			firstTickOccurred = true;
		}

		final var isFocused = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED) == TRUE;

		if (wasFocused && !isFocused) minimize();
		else if (!wasFocused && isFocused) maximize();

		wasFocused = isFocused;
	}

	private void minimize() {
		originalVolume = options.getSoundVolume(SoundCategory.MASTER);
		options.setSoundVolume(SoundCategory.MASTER, VOLUME_ZERO);
		LOG.info("window unfocused; volume muted");
	}

	private void maximize() {
		options.setSoundVolume(SoundCategory.MASTER, originalVolume);
		LOG.info(String.format("window refocused; volume restored to %.2f", originalVolume));
	}
}