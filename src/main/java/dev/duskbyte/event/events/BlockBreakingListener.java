package dev.duskbyte.event.events;

import dev.duskbyte.event.CancellableEvent;
import dev.duskbyte.event.Listener;

import java.util.ArrayList;

public interface BlockBreakingListener extends Listener {
	void onBlockBreaking(BlockBreakingEvent event);

	class BlockBreakingEvent extends CancellableEvent<BlockBreakingListener> {

		@Override
		public void fire(ArrayList<BlockBreakingListener> listeners) {
			listeners.forEach(e -> e.onBlockBreaking(this));
		}

		@Override
		public Class<BlockBreakingListener> getListenerType() {
			return BlockBreakingListener.class;
		}
	}
}
