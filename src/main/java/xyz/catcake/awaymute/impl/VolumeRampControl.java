package xyz.catcake.awaymute.impl;

import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.log.PrefixedMessageFactory;

final class VolumeRampControl {

	private static final Logger log;

	static { log = LogManager.getLogger(new PrefixedMessageFactory(AwayMuteMod.LOG_PREFIX)); }

	private final int         duration;
	private final float       volumeMinimum;
	private final float       volumeMaximum;
	private final GameOptions options;

	private boolean      ramping;
	private int          rampPosition;
	private float        volumeDestination;
	private float        volumeRampIncrement;
	private VolumeAction volumeCurrentAction;

	public VolumeRampControl(
			final float volumeMinimum, final float volumeMaximum, final int duration, final GameOptions options) {
		this.duration       = duration;
		this.volumeMinimum  = volumeMinimum;
		this.volumeMaximum  = volumeMaximum;
		this.options        = options;
		ramping             = false;
		rampPosition        = 0;
		volumeDestination   = 0;
		volumeRampIncrement = 0;
		volumeCurrentAction = VolumeAction.NONE;
	}

	/** Call this every game tick to progress the state of {@link VolumeRampControl}. */
	public void tick() {
		switch (volumeCurrentAction) {
		case NONE -> { /* Do nothing if no action is currently being performed. */ }
		case RAMP_DOWN, RAMP_UP -> performRamp();
		}
	}

	public void rampDown(final float volumeDestination) { ramp(VolumeAction.RAMP_DOWN, volumeDestination); }

	public void rampUp(final float volumeDestination) { ramp(VolumeAction.RAMP_UP, volumeDestination); }

	private void ramp(final VolumeAction volumeAction, final float volumeDestination) {
		if (!validVolume(volumeDestination)) {
			log.error("Ignoring ramp request. Erroneous volumeDestination");
			return;
		}
		if (ramping) handleBadFloatState();
		ramping                = true;
		this.volumeDestination = volumeDestination;
		volumeRampIncrement    = Math.abs(options.getSoundVolume(SoundCategory.MASTER) - volumeDestination) / duration;
		volumeCurrentAction    = volumeAction;
	}

	private void performRamp() {
		final float volumeCurrent = options.getSoundVolume(SoundCategory.MASTER);
		log.debug("ramping... curVol: " + volumeCurrent + " destVol: " + volumeDestination + " pos: " + rampPosition);
		switch (volumeCurrentAction) {
		case RAMP_DOWN -> {
			if (volumeCurrent - volumeRampIncrement >= volumeMinimum) break;
			handleBadFloatState();
			return;
		}
		case RAMP_UP -> {
			if (volumeCurrent + volumeRampIncrement <= volumeMaximum) break;
			handleBadFloatState();
			return;
		}}
		if (rampPosition < duration) rampInternal(volumeCurrent);
		else reset();
	}

	/**
	 * This is called more frequently than one would think...
	 * It's about a 50/50 between this being called towards the end of a ramp &
	 * the ramp finishing naturally by doing each increment. Floating point number are Cool.
	 * It basically finished the ramp by instantly setting the volume to its destination &
	 * then calling reset.
	 */
	private void handleBadFloatState() {
		options.setSoundVolume(SoundCategory.MASTER, volumeDestination);
		reset();
	}

	private void reset() {
		ramping             = false;
		rampPosition        = 0;
		volumeRampIncrement = 0;
		volumeCurrentAction = VolumeAction.NONE;
		// Ensure volume is a sensible number and not 0.5033113
		options.setSoundVolume(SoundCategory.MASTER,
		                       Math.round(options.getSoundVolume(SoundCategory.MASTER) * 100) / 100f);
	}

	private void rampInternal(final float volumeCurrent) {
		switch (volumeCurrentAction) {
		case RAMP_DOWN -> options.setSoundVolume(SoundCategory.MASTER, volumeCurrent - volumeRampIncrement);
		case RAMP_UP -> options.setSoundVolume(SoundCategory.MASTER, volumeCurrent + volumeRampIncrement);
		}
		++rampPosition;
	}

	private boolean validVolume(final float volumeDestination) {
		if (volumeDestination < volumeMinimum) return false;
		return !(volumeDestination > volumeMaximum);
	}
}