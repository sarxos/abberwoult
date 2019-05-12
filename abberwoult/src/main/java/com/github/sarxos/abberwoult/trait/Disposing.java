package com.github.sarxos.abberwoult.trait;

import java.util.function.Supplier;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import io.vavr.control.Option;


public interface Disposing extends ActorInternal {

	/**
	 * Dispose actor referenced by a given {@link ActorRef}.
	 *
	 * @param ref the {@link ActorRef} to dispose
	 */
	default void dispose(final ActorRef ref) {
		if (ref != null) {
			ref.tell(PoisonPill.getInstance(), self());
		} else {
			throw new NullPointerException("Actor reference to dispose must not be null!");
		}
	}

	/**
	 * Optionally dispose actor referenced by a given {@link ActorRef}.
	 *
	 * @param ref the {@link ActorRef} to dispose
	 */
	default void dispose(final Option<ActorRef> ref) {
		ref.forEach(this::dispose);
	}

	/**
	 * Dispose actor referenced by a supplied {@link ActorRef}.
	 *
	 * @param ref the {@link ActorRef} to dispose
	 */
	default void dispose(final Supplier<ActorRef> ref) {
		dispose(ref.get());
	}

	/**
	 * Dispose this actor (a self).
	 */
	default void dispose() {
		dispose(self());
	}
}
