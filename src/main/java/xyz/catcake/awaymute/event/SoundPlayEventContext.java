package xyz.catcake.awaymute.event;

import net.minecraft.client.option.GameOptions;
import xyz.catcake.event.EventContext;

public final class SoundPlayEventContext extends EventContext {
	private final GameOptions settings;

	public SoundPlayEventContext(final GameOptions settings) {
		this.settings = settings;
	}

	public GameOptions getSettings() {
		return settings;
	}
}