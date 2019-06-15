package com.github.sarxos.abberwoult;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.event.EventStream;


/**
 * This is class used to move events from CDI notifiers into the events stream exposed by the
 * actor system.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class EventsBypass {

	/**
	 * The {@link EventStream} exposed by the {@link ActorSystem}.
	 */
	private final EventStream stream;

	/**
	 * @param system the {@link ActorSystem}
	 */
	@Inject
	public EventsBypass(final ActorSystem system) {
		this.stream = system.getEventStream();
	}

	/**
	 * Interceptor method to capture all events emitted by CDI notifiers. Please note that
	 * this method will filter off all {@link String} events.
	 *
	 * @param event the event
	 */
	public void bypass(@Observes Object event) {

		// Drop all string events, we don't want. Them are emitted because CDI specification
		// requirement, but they do not really carry any useful information.

		if (event instanceof String) {
			return;
		}

		stream.publish(event);
	}
}
