package com.github.sarxos.abberwoult.dsl;

import static java.util.Objects.requireNonNull;

import akka.actor.ActorSystem;
import akka.event.EventStream;


public interface Events extends ActorInternal {

	/**
	 * Subscribe to all events of a given class propagated via {@link EventStream}.
	 *
	 * @param clazz the subscribed events class
	 */
	default void eventSubscribe(final Class<?> clazz) {
		eventStream().subscribe(self(), requireNonNull(clazz, "The event class to subscribe must not be null!"));
	}

	/**
	 * Revoke subscription to all events of a given class propagated via {@link EventStream}.
	 *
	 * @param clazz the class of event to revoke subscription for
	 */
	default void eventUnsubscribe(final Class<?> clazz) {
		eventStream().unsubscribe(self(), requireNonNull(clazz, "The event class to unsubscribe must not be null!"));
	}

	/**
	 * Publish event to the {@link EventStream}.
	 *
	 * @param event the event to be published
	 */
	default void eventPublish(final Object event) {
		eventStream().publish(requireNonNull(event, "Published event must not be null!"));
	}

	/**
	 * @return The {@link EventStream} provided by the running {@link ActorSystem}.
	 */
	default EventStream eventStream() {
		return getContext()
			.system()
			.getEventStream();
	}
}
