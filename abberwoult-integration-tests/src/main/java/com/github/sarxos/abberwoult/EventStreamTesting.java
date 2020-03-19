package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.Event;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Events;
import com.github.sarxos.abberwoult.dsl.Utils;

import akka.actor.ActorRef;


public class EventStreamTesting {

	public static class Ready {
	}

	public static class Bobek {
	}

	public static class Mumin {
	}

	public static class Noone {
	}

	public static final class EventStreamTestActor extends SimpleActor implements Events, Utils {

		private final ActorRef probe;

		public EventStreamTestActor(final ActorRef probe) {
			this.probe = probe;
		}

		public void handleReady(@Receives Ready msg) {
			forward(probe, msg);
		}

		public void handleEvent1(@Receives @Event Bobek b) {
			forward(probe, b);
		}

		public void handleEvent2(@Receives @Event Mumin m) {
			forward(probe, m);
		}
	}
}
