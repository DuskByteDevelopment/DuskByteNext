
package dev.duskbyte.mixin;

import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.module.player.FastPlace;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTickHead(CallbackInfo ci) {
        FastPlace fastPlace = (FastPlace) ModuleManager.get(FastPlace.class);
        if (fastPlace != null && fastPlace.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            // itemUseCooldown is accessed via mixin - use reflection-free approach
            // We set the field directly since we're inside the mixin
            ((MinecraftClientAccessor) mc).setItemUseCooldown(0);
        }
    }
}
