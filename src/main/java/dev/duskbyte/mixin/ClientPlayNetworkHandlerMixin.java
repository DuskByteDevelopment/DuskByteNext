
package dev.duskbyte.mixin;

import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.module.combat.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onEntityVelocity", at = @At("HEAD"))
    private void onEntityVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        Velocity velocity = (Velocity) ModuleManager.get(Velocity.class);
        if (velocity != null && velocity.isEnabled() && mc.player != null) {
            if (packet.getId() == mc.player.getId()) {
                ci.cancel();
            }
        }
    }
}
