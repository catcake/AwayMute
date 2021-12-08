package xyz.catcake.awaymute.impl;

import net.minecraft.client.option.GameOptions;
import net.minecraft.sound.SoundCategory;

final class VolumeRampControl {
    private final int duration;
    private final float volumeMinimum;
    private final float volumeMaximum;
    private final GameOptions options;

    private int position;
    private float volumeDestination;
    private VolumeAction volumeCurrentAction;

    // TODO: volume is instantly being set to 0 and is not restoring to original volume.
    public VolumeRampControl(final float volumeMinimum, final float volumeMaximum, final int duration, final GameOptions options) {
        this.duration = duration;
        this.volumeMinimum = volumeMinimum;
        this.volumeMaximum = volumeMaximum;
        this.options = options;
        position = 0;
        volumeDestination = 0;
        volumeCurrentAction = VolumeAction.NONE;
    }

    public boolean ramping() { return volumeCurrentAction != VolumeAction.NONE; }

    public void tick() {
        switch (volumeCurrentAction) {
            case NONE -> {} // Do nothing if no action is currently being performed.
            case RAMP_DOWN, RAMP_UP -> rampInternal();
        }
    }

    public void rampDown(final float volumeDestination) throws  IllegalArgumentException {
        checkValidVolume(volumeDestination);
        this.volumeDestination = volumeDestination;
        volumeCurrentAction = VolumeAction.RAMP_DOWN;
    }

    public void rampUp(final float volumeDestination) throws IllegalArgumentException {
        checkValidVolume(volumeDestination);
        this.volumeDestination = volumeDestination;
        volumeCurrentAction = VolumeAction.RAMP_UP;
    }

    private void checkValidVolume(final float volumeDestination) throws IllegalArgumentException {
        if (volumeDestination < volumeMinimum)
            throw new IllegalArgumentException("volumeDestination cannot be greater than volumeMinimum");
        if (volumeDestination > volumeMaximum)
            throw new IllegalArgumentException("volumeDestination cannot be less than volumeMaximum");
    }

    private void rampInternal() {
        final var volumeCurrent = options.getSoundVolume(SoundCategory.MASTER);
        if (volumeCurrent < volumeMinimum || volumeCurrent > volumeDestination) {
            handleBadState();
            return;
        }

        if (position < duration) {
            final var changeIncrement = Math.abs(volumeCurrent - volumeDestination) / duration;
            switch (volumeCurrentAction) {
                // ramp() shouldn't really be called when no action is being performed,
                // but it doesn't hurt anything.
                case NONE -> {}
                case RAMP_DOWN -> options.setSoundVolume(SoundCategory.MASTER, volumeCurrent - changeIncrement);
                case RAMP_UP -> options.setSoundVolume(SoundCategory.MASTER, volumeCurrent + changeIncrement);
            }
            ++position;
        } else reset();
    }

    private void handleBadState() {
        options.setSoundVolume(SoundCategory.MASTER, volumeDestination);
        reset();
    }

    private void reset() {
        position = 0;
        volumeCurrentAction = VolumeAction.NONE;
    }
}