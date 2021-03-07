package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Events;
import com.github.sarxos.abberwoult.dsl.Utils;

import akka.actor.ActorRef;


public class EventsBypassTesting {

	public static class Ready {
	}

	public static class Event1 {
	}

	public static class Event2 {
	}

	public static class Event3 {
	}

	public static final class EventBypassTestActor extends SimpleActor implements Events, Utils {

		private final ActorRef probe;

		public EventBypassTestActor(final ActorRef probe) {
			this.probe = probe;
		}

		@PreStart
		public void setup() {
			eventSubscribe(Event1.class);
			eventSubscribe(Event2.class);
		}

		public void handleReady(@Receives Ready msg) {
			forward(msg, probe);
		}

		public void handleEvent1(@Receives Event1 event) {
			forward(event, probe);
		}

		public void handleEvent2(@Receives Event2 event) {
			forward(event, probe);
		}
	}
}
