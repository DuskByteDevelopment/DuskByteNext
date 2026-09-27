package dev.duskbyte;

import net.fabricmc.api.ClientModInitializer;

import java.io.IOException;

public final class Main implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		try {
			new DuskByte();
		} catch (InterruptedException | IOException ignored) {}
	}
}
