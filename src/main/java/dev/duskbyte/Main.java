package dev.duskbyte;

import dev.duskbyte.gui.AuthDialog;
import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;

public final class Main implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		try {
			new DuskByte();
		} catch (InterruptedException | IOException ignored) {}

		// 先弹 JDialog 登录/注册,成功后才继续加载游戏
		// (Mojang 加载画面 → MC 标题界面);本地 token 有效时不会弹窗
		if (!AuthDialog.promptBlocking()) {
			System.exit(0);
		}
	}
}
