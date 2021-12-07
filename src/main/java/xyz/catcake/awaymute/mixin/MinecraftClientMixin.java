package xyz.catcake.awaymute.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.catcake.awaymute.AwayMuteMod;
import xyz.catcake.awaymute.event.MaximizedEventContext;
import xyz.catcake.awaymute.event.MinimizedEventContext;

@Mixin(MinecraftClient.class)
public final class MinecraftClientMixin {

    @Shadow @Final public GameOptions options;

    private boolean firstTickOccurred;
    private boolean wasFocused;
    private long windowHandle;

    public MinecraftClientMixin() { firstTickOccurred = false; }

    private void firstTick() {
        windowHandle = MinecraftClient.getInstance().getWindow().getHandle();
        wasFocused = true;
        firstTickOccurred = true;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo callbackInfo) {
        if (!firstTickOccurred) firstTick();

        final var isFocused = GLFW.glfwGetWindowAttrib(windowHandle, GLFW.GLFW_FOCUSED) == 1;
        if (wasFocused && !isFocused)
            AwayMuteMod.getEventManager().publish(new MinimizedEventContext(options));
        else if (!wasFocused && isFocused)
            AwayMuteMod.getEventManager().publish(new MaximizedEventContext(options));

        wasFocused = isFocused;
    }
}
