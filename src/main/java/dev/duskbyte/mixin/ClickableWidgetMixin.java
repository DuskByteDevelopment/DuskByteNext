package dev.duskbyte.mixin;

import dev.duskbyte.utils.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.Color;

/**
 * 标题界面按钮美化。
 *
 * 在标题界面时,用自定义圆角按钮(填充 + 边框 + 悬停高亮)替换原版
 * ClickableWidget 的绘制;其他界面完全走原版渲染,不受影响。
 *
 * 注入点选在 ClickableWidget.render —— 它是 Drawable 接口方法在
 * 控件层级里的实现处(yarn 映射文件不列出接口实现,但字节码里存在),
 * 在这里 cancel 即可整体接管按钮的背景与文字绘制。
 */
@Mixin(ClickableWidget.class)
public abstract class ClickableWidgetMixin {

	@Shadow
	protected int x;

	@Shadow
	protected int y;

	@Shadow
	protected int width;

	@Shadow
	protected int height;

	@Shadow
	protected Text message;

	@Shadow
	protected boolean hovered;

	@Shadow
	protected boolean active;

	@Shadow
	protected boolean visible;

	@Inject(method = "render(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
	private void duskbyte$styledTitleButton(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (!(mc.currentScreen instanceof TitleScreen)) return;

		// 不可见:与原版一致,什么都不画
		if (!this.visible) {
			ci.cancel();
			return;
		}

		// ---- 状态配色 ----
		Color fill;
		Color border;
		int textColor;
		if (!this.active) {
			// 禁用(如演示模式下的按钮)
			fill = new Color(28, 29, 34, 150);
			border = new Color(64, 64, 70, 130);
			textColor = new Color(150, 150, 155, 255).getRGB();
		} else if (this.hovered) {
			// 悬停:提亮 + 红色描边(DuskByte 主题色)
			fill = new Color(60, 63, 82, 245);
			border = new Color(255, 96, 96, 255);
			textColor = 0xFFFFFF;
		} else {
			// 常规
			fill = new Color(34, 36, 45, 225);
			border = new Color(92, 96, 112, 210);
			textColor = new Color(230, 230, 235, 255).getRGB();
		}

		// ---- 圆角边框(外层) + 填充(内层),复用 ClickGui 同款渲染 ----
		RenderUtils.renderRoundedQuad(context.getMatrices(), border,
				this.x, this.y, this.x + this.width, this.y + this.height,
				5, 5, 5, 5, 20);
		RenderUtils.renderRoundedQuad(context.getMatrices(), fill,
				this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1,
				4, 4, 4, 4, 20);

		// ---- 居中文字(带原版阴影) ----
		String label = this.message.getString();
		int textX = this.x + (this.width - mc.textRenderer.getWidth(label)) / 2;
		int textY = this.y + (this.height - mc.textRenderer.fontHeight) / 2 + 1;
		context.drawText(mc.textRenderer, label, textX, textY, textColor, true);

		ci.cancel();
	}
}
