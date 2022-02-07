package xyz.catcake.awaymute.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.awaymute.event.TickEndEventContext;

@Mixin(MinecraftClient.class)
public final class MinecraftClientMixin {

	@Inject(method = "tick", at = @At("RETURN"))
	private void tick(final CallbackInfo callbackInfo) { AwayMuteMod.Instance().publishEvent(new TickEndEventContext()); }
}