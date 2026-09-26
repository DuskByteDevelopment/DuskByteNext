
package dev.duskbyte.mixin;

import dev.duskbyte.module.ModuleManager;
import dev.duskbyte.module.misc.ChatSuffix;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ChatMixin {
    @Unique
    private static boolean isSendingModifiedMessage = false;

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        if (isSendingModifiedMessage) return;

        ChatSuffix chatSuffix = (ChatSuffix) ModuleManager.get(ChatSuffix.class);
        if (chatSuffix != null && chatSuffix.isEnabled()) {
            String suffix = "";
            for (var setting : chatSuffix.getSettings()) {
                if (setting.getName().equals("Suffix")) {
                    suffix = ((dev.duskbyte.setting.StringSetting) setting).get();
                    break;
                }
            }

            if (!suffix.isEmpty() && message.length() < 256 - suffix.length()) {
                String newMessage = message + suffix;
                ci.cancel();
                isSendingModifiedMessage = true;
                ((ClientPlayNetworkHandler) (Object) this).sendChatMessage(newMessage);
                isSendingModifiedMessage = false;
            }
        }
    }
}
