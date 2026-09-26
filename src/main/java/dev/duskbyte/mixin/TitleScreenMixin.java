
package dev.duskbyte.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        context.drawTextWithShadow(
            mc.textRenderer,
            "§b§lDuskByte Client §7v" + dev.duskbyte.DuskByteClient.VERSION,
            4, mc.textRenderer.fontHeight + 4,
            0xFFFFFF
        );
    }
}
