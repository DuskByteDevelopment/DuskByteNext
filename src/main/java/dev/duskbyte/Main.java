package dev.duskbyte;

import dev.duskbyte.gui.AuthDialog;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Main implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("DuskByte-Auth");

	@Override
	public void onInitializeClient() {
		// 必须在任何 AWT/Swing 类初始化之前执行:
		// 启动器可能带了 -Djava.awt.headless=true,不覆盖掉的话 JDialog 会抛
		// HeadlessException("不弹登录窗口")。GraphicsEnvironment 只在首次加载时读取该属性。
		System.setProperty("java.awt.headless", "false");

		LOGGER.info("client entrypoint started");

		try {
			new DuskByte();
			LOGGER.info("DuskByte initialized, auto-login authenticated={}",
					DuskByte.INSTANCE != null && DuskByte.INSTANCE.authenticated);
		} catch (Throwable t) {
			// 之前只 catch InterruptedException/IOException:构造函数若抛出运行时异常,
			// 会直接冒泡到 Fabric(Fabric 吞掉异常并继续加载游戏),
			// 导致后面的登录窗口代码根本不会执行 → "游戏正常进,但不弹登录窗口"。
			LOGGER.error("DuskByte initialization failed", t);
		}

		// 先弹 JDialog 登录/注册,成功后才继续加载游戏
		// (Mojang 加载画面 → MC 标题界面);本地 token 有效时不会弹窗
		boolean ok = AuthDialog.promptBlocking();
		LOGGER.info("promptBlocking returned {}", ok);
		if (!ok) {
			// JDialog 彻底弹不出来时不退出游戏,放行加载,
			// 由 MinecraftClientMixin 兜底的游戏内 AuthScreen 完成登录
			LOGGER.warn("JDialog login unavailable, falling back to in-game auth screen");
		}
	}
}
