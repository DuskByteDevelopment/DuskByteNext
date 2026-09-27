package dev.duskbyte;

import net.fabricmc.api.ModInitializer;

import java.io.IOException;
import java.net.URISyntaxException;

public final class Main implements ModInitializer {
	@Override
	public void onInitialize() {
		try {
			new DuskByte();
		} catch (InterruptedException | IOException ignored) {}
	}
}
