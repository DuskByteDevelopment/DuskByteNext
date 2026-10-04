package dev.duskbyte.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
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
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 标题界面自定义背景(项目根目录的 bg.jpg)。
 *
 * 由 ScreenMixin 在 Screen.renderBackground 中调用,替换原版全景背景。
 * 图片加载顺序:
 *   1. mod 资源: assets/duskbyte/textures/title_bg.jpg (打进 jar,正式分发用)
 *   2. 回退: 游戏工作目录下的 bg.jpg (开发期临时用)
 *   3. 都没有 → 返回 false,保留原版背景
 *
 * JPEG 由 ImageIO 解码后转成 PNG 字节,再交给 NativeImage
 * (NativeImage 只认 PNG,参考 GlyphPage 的做法)。
 */
public final class TitleBackground {
    private static final Logger LOGGER = LoggerFactory.getLogger(TitleBackground.class);
    private static final String RESOURCE_PATH = "/assets/duskbyte/textures/title_bg.jpg";

    private static boolean initialized = false;
    private static boolean loaded = false;
    private static NativeImageBackedTexture texture;

    private TitleBackground() {
    }

    /**
     * 绘制全屏自定义背景。
     *
     * @return true = 已绘制(调用方应 cancel 原版背景);false = 图片不可用(保留原版)
     */
    public static boolean draw(DrawContext context, int width, int height) {
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
        buffer.vertex(matrix, 0, height, 0).color(1f, 1f, 1f, 1f).texture(0f, 1f);
        buffer.vertex(matrix, width, height, 0).color(1f, 1f, 1f, 1f).texture(1f, 1f);
        buffer.vertex(matrix, width, 0, 0).color(1f, 1f, 1f, 1f).texture(1f, 0f);
        buffer.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0f, 0f);
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.disableBlend();
        return true;
    }

    private static boolean load() {
        try (InputStream in = TitleBackground.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in != null) {
                return loadFrom(ImageIO.read(in));
            }

            // 回退: 工作目录下的 bg.jpg
            File fallback = new File("bg.jpg");
            if (fallback.isFile()) {
                LOGGER.info("[TitleBackground] mod 资源里没有 {},使用工作目录 {}", RESOURCE_PATH, fallback.getAbsolutePath());
                return loadFrom(ImageIO.read(fallback));
            }

            LOGGER.warn("[TitleBackground] 找不到背景图 {} (也没有 ./bg.jpg),标题界面保持原版背景", RESOURCE_PATH);
            return false;
        } catch (Exception e) {
            LOGGER.error("[TitleBackground] 背景图加载失败", e);
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
        LOGGER.info("[TitleBackground] 标题背景已加载 ({}x{})", image.getWidth(), image.getHeight());
        return true;
    }
}
