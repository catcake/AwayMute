package xyz.catcake.awaymute.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.awaymute.event.SoundPlayEventContext;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {
	@Shadow
	@Final
	private GameOptions settings;

	@Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"))
	private void onSoundPlay(CallbackInfo callbackInfo) {
		AwayMuteMod.getEventManager().publish(new SoundPlayEventContext(settings));
	}
}