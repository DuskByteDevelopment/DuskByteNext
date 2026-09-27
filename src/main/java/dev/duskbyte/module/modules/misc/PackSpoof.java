package dev.duskbyte.module.modules.misc;

import dev.duskbyte.event.events.PacketReceiveListener;
import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;

public class PackSpoof extends Module implements PacketReceiveListener {
    public PackSpoof() {
        super("Pack Spoof", "Ignores custom resource packs", -1, Category.MISC);
    }

    @Override
    public void onEnable() {
        eventManager.add(PacketReceiveListener.class, this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        eventManager.remove(PacketReceiveListener.class, this);
        super.onDisable();
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if(mc.getNetworkHandler() != null) {
            Packet<?> packet = event.packet;
            if (packet instanceof ResourcePackSendS2CPacket) {
                event.cancel();

                mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.ACCEPTED));
                mc.getNetworkHandler().sendPacket(new ResourcePackStatusC2SPacket(mc.player.getUuid(), ResourcePackStatusC2SPacket.Status.SUCCESSFULLY_LOADED));
            }
        }
    }
}
