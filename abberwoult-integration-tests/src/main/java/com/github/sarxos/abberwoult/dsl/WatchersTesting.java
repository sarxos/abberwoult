package com.github.sarxos.abberwoult.dsl;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.ActorRef;


public class WatchersTesting {

	public static class WatchMsg {
	}

	public static class WatchedActor extends SimpleActor {

	}

	public static class WatchingActor extends SimpleActor implements Watchers, Utils {

		final AtomicBoolean terminated;
		ActorRef watched;

		public WatchingActor(final AtomicBoolean terminated) {
			this.terminated = terminated;
		}

		@PreStart
		public void setup() {
			this.watched = actor().of(WatchedActor.class).create();
		}

		@Override
		public void onActorTerminated(final ActorRef ref) {
			if (ActorUtils.equals(watched, ref)) {
				terminated.set(true);
			}
		}

		public void handleWatchMsg(@Received WatchMsg msg) {
			watch(watched);
		}
	}
}
