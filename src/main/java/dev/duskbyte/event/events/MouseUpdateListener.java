package dev.duskbyte.event.events;

import dev.duskbyte.event.Event;
import dev.duskbyte.event.Listener;

import java.util.ArrayList;

public interface MouseUpdateListener extends Listener {
	void onMouseUpdate();

	class MouseUpdateEvent extends Event<MouseUpdateListener> {
		@Override
		public void fire(ArrayList<MouseUpdateListener> listeners) {
			listeners.forEach(MouseUpdateListener::onMouseUpdate);
		}

		@Override
		public Class<MouseUpdateListener> getListenerType() {
			return MouseUpdateListener.class;
		}
	}
}
