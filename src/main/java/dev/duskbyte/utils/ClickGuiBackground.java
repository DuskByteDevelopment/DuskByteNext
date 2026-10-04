package dev.duskbyte.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * ClickGUI 自定义背景渲染工具，加载 assets/duskbyte/textures/background.jpg 并全屏拉伸绘制。
 */
public final class ClickGuiBackground {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClickGuiBackground.class);
    private static final String RESOURCE_PATH = "/assets/duskbyte/textures/background.jpg";

    private static boolean initialized = false;
    private static boolean loaded = false;
    private static NativeImageBackedTexture texture;

    private ClickGuiBackground() {
    }

    /**
     * 绘制全屏自定义背景。
     *
     * @return true = 已绘制; false = 图片不可用
     */
    public static boolean draw(DrawContext context, int width, int height, float alpha) {
        if (!initialized) {
            initialized = true;
            loaded = load();
        }
        if (!loaded || texture == null) return false;

        MatrixStack matrices = context.getMatrices();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, texture.getGlId());

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix, 0, height, 0).color(1f, 1f, 1f, alpha).texture(0f, 1f);
        buffer.vertex(matrix, width, height, 0).color(1f, 1f, 1f, alpha).texture(1f, 1f);
        buffer.vertex(matrix, width, 0, 0).color(1f, 1f, 1f, alpha).texture(1f, 0f);
        buffer.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, alpha).texture(0f, 0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
        return true;
    }

    private static boolean load() {
        try (InputStream in = ClickGuiBackground.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in != null) {
                return loadFrom(ImageIO.read(in));
            }
            LOGGER.warn("[ClickGuiBackground] 找不到背景图 {}", RESOURCE_PATH);
            return false;
        } catch (Exception e) {
            LOGGER.error("[ClickGuiBackground] 背景图加载失败", e);
            return false;
        }
    }

    private static boolean loadFrom(BufferedImage image) throws Exception {
        if (image == null) return false;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        ByteBuffer data = BufferUtils.createByteBuffer(baos.size()).put(baos.toByteArray());
        data.flip();

        texture = new NativeImageBackedTexture(NativeImage.read(data));
        LOGGER.info("[ClickGuiBackground] ClickGUI背景已成功加载 ({}x{})", image.getWidth(), image.getHeight());
        return true;
    }
}
