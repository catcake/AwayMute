package xyz.catcake.awaymute;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import org.lwjgl.glfw.GLFW;

public final class AwayMute {
	private float originalVolume;
	private boolean wasFocused;

	public AwayMute() {
		originalVolume = 1;
		wasFocused = true;
	}

	public void execute(final GameOptions settings) {
		final var windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
		final var isFocused = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED) == 1;

		if (wasFocused && !isFocused) mute(settings);
		else if (!wasFocused && isFocused) unmute(settings);
	}

	private void mute(final GameOptions settings) {
		originalVolume = settings.getSoundVolume(SoundCategory.MASTER);
		settings.setSoundVolume(SoundCategory.MASTER, 0);
		AwayMuteMod.info("window unfocused; volume muted");
		wasFocused = false;
	}

	private void unmute(final GameOptions settings) {
		settings.setSoundVolume(SoundCategory.MASTER, originalVolume);
		AwayMuteMod.info(String.format("window refocused; volume restored to %.2f", originalVolume));
		wasFocused = true;
	}
}