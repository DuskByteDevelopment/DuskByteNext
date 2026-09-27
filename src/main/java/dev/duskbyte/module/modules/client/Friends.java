package dev.duskbyte.module.modules.client;

import dev.duskbyte.DuskByte;
import dev.duskbyte.event.events.AttackListener;
import dev.duskbyte.event.events.ButtonListener;
import dev.duskbyte.event.events.HudListener;
import dev.duskbyte.managers.FriendManager;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.module.setting.BooleanSetting;
import dev.duskbyte.module.setting.KeybindSetting;
import dev.duskbyte.utils.RenderUtils;
import dev.duskbyte.utils.TextRenderer;
import dev.duskbyte.utils.WorldUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public final class Friends extends Module implements ButtonListener, AttackListener, HudListener {
    private final KeybindSetting addFriendKey = new KeybindSetting("Friend Key", GLFW.GLFW_MOUSE_BUTTON_MIDDLE, false)
            .setDescription("Key to add/remove friends");
    public final BooleanSetting antiAttack = new BooleanSetting("Anti-Attack", false)
            .setDescription("Doesn't let you hit friends");
    public final BooleanSetting disableAimAssist = new BooleanSetting("Anti-Aim", false)
            .setDescription("Disables aim assist for friends");
    public final BooleanSetting friendStatus = new BooleanSetting("Friend Status", false)
            .setDescription("Tells you if you're aiming at a friend or not");

    private FriendManager manager;

    public Friends() {
        super("Friends", "This module makes it so you can't do certain stuff if you have a player friended!", -1, Category.CLIENT);
        addSettings(addFriendKey, antiAttack, disableAimAssist, friendStatus);
        setKey(-1);
    }

    @Override
    public void onEnable() {
        manager = DuskByte.INSTANCE.getFriendManager();

        eventManager.add(ButtonListener.class, this);
        eventManager.add(AttackListener.class, this);
        eventManager.add(HudListener.class, this);

        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(ButtonListener.class, this);
        eventManager.remove(AttackListener.class, this);
        eventManager.remove(HudListener.class, this);

        super.onDisable();
    }

    @Override
    public void onButtonPress(ButtonEvent event) {
        if(mc.player == null)
            return;

        if(mc.currentScreen != null)
            return;

        if(mc.crosshairTarget instanceof EntityHitResult hitResult) {
            Entity entity = hitResult.getEntity();

            if(entity instanceof PlayerEntity player) {
                if (event.button == addFriendKey.getKey() && event.action == GLFW.GLFW_PRESS) {
                    if(!manager.isFriend(player))
                        manager.addFriend(player);
                    else manager.removeFriend(player);
                }
            }
        }
    }

    @Override
    public void onAttack(AttackEvent event) {
        if(!antiAttack.getValue())
            return;

        if(manager.isAimingOverFriend())
            event.cancel();
    }

    @Override
    public void onRenderHud(HudEvent event) {
        if(!friendStatus.getValue())
            return;

        RenderUtils.unscaledProjection();
        if(WorldUtils.getHitResult(100) instanceof EntityHitResult hitResult) {
            Entity entity = hitResult.getEntity();
            DrawContext context = event.context;

            if(entity instanceof PlayerEntity player) {
                if(manager.isFriend(player)) {
                    TextRenderer.drawCenteredString("Player is friend", context, (mc.getWindow().getWidth() / 2), (mc.getWindow().getHeight() / 2) + 25, Color.GREEN.getRGB());
                }
            }
        }
        RenderUtils.scaledProjection();
    }
}
