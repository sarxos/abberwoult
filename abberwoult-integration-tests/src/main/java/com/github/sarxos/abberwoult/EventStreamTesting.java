package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.Event;
import com.github.sarxos.abberwoult.annotation.Received;
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

		public void handleReady(@Received Ready msg) {
			forward(msg, probe);
		}

		public void handleEvent1(@Received @Event Bobek b) {
			forward(b, probe);
		}

		public void handleEvent2(@Received @Event Mumin m) {
			forward(m, probe);
		}
	}
}
