package xyz.catcake.awaymute.event;

import net.minecraft.client.option.GameOptions;

public record SoundPlayEventContext(GameOptions settings) {}