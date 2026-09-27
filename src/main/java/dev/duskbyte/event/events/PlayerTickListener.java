package dev.duskbyte.event.events;

import dev.duskbyte.event.Event;
import dev.duskbyte.event.Listener;

import java.util.ArrayList;

public interface PlayerTickListener extends Listener {
	void onPlayerTick();

	class PlayerTickEvent extends Event<PlayerTickListener> {
		@Override
		public void fire(ArrayList<PlayerTickListener> listeners) {
			listeners.forEach(PlayerTickListener::onPlayerTick);
		}

		@Override
		public Class<PlayerTickListener> getListenerType() {
			return PlayerTickListener.class;
		}
	}
}
