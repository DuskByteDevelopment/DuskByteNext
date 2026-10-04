package dev.duskbyte.mixin;

import dev.duskbyte.gui.ClickGui;
import dev.duskbyte.utils.TitleBackground;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {

	@Shadow
	@Nullable
	protected MinecraftClient client;

	@Shadow
	protected int width;

	@Shadow
	protected int height;

	@Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
	private void dontRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.client.currentScreen instanceof ClickGui) {
			ci.cancel();
			return;
		}

		// 标题界面: 用 bg.jpg 替换原版全景背景(yarn 1.21.1 中全景/模糊/暗化
		// 全部经由 Screen.renderBackground,而 TitleScreen 并未重写它)。
		// 图片不可用时返回 false,不 cancel,保留原版背景。
		if (this.client.currentScreen instanceof TitleScreen
				&& TitleBackground.draw(context, this.width, this.height)) {
			ci.cancel();
		}
	}
}
