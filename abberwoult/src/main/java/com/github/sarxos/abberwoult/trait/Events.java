package com.github.sarxos.abberwoult.trait;

import static java.util.Objects.requireNonNull;

import com.github.sarxos.abberwoult.trait.internal.InActor;

import akka.actor.ActorSystem;
import akka.event.EventStream;


public interface Events extends InActor {

	/**
	 * Subscribe to all events of a given class propagated via system {@link EventStream}.
	 *
	 * @param clazz the subscribed events class
	 */
	default void eventSubscribe(final Class<?> clazz) {
		eventStream().subscribe(self(), requireNonNull(clazz, "The class of event to subscribe to must not be null!"));
	}

	/**
	 * Revoke subscription to all events of a given class propagated via system {@link EventStream}.
	 *
	 * @param clazz the class of event to revoke subscription for
	 */
	default void eventUnsubscribe(final Class<?> clazz) {
		eventStream().unsubscribe(self(), requireNonNull(clazz, "The class of event to unsubscribe from must not be null!"));
	}

	/**
	 * Publish event to the system {@link EventStream}.
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
		return context()
			.system()
			.getEventStream();
	}
}
