
package dev.duskbyte.module.misc;

import dev.duskbyte.module.Category;
import dev.duskbyte.module.Module;
import dev.duskbyte.setting.StringSetting;

public class ChatSuffix extends Module {
    private final StringSetting suffix = add(new StringSetting("Suffix", " | DuskByte"));

    public ChatSuffix() {
        super("ChatSuffix", "Appends suffix to chat messages", Category.MISC);
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}
}
