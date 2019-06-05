package com.github.sarxos.abberwoult.trait;

import com.github.sarxos.abberwoult.annotation.Received;

import akka.actor.ActorRef;
import akka.actor.Terminated;


public interface Watcher extends ActorInternal {

	/**
	 * Watch given {@link ActorRef}.
	 *
	 * @param ref the {@link ActorRef} to be watched
	 */
	default void watch(final ActorRef ref) {
		context().watch(ref);
	}

	/**
	 * Unwatch given {@link ActorRef}.
	 *
	 * @param ref the {@link ActorRef} to unwatch
	 * @return Return unwatched {@link ActorRef}
	 */
	default void unwatch(final ActorRef ref) {
		context().unwatch(ref);
	}

	/**
	 * Handles watched actor termination. One can override this method when needed but please make
	 * sure to keep in mind that after {@link Terminated} is received it will downstream and invoke
	 * {@link #onActorTerminated(ActorRef)} method, so in case when this is overridden, the
	 * downstream method may never be invoked.
	 *
	 * @param terminated the {@link Terminated} message received from a system dead watch
	 */
	default void handleTerminated(@Received Terminated terminated) {
		onActorTerminated(terminated.actor());
	}

	/**
	 * This callback is invoked when watched actor was terminated. The actor is terminated whenever
	 * it becomes dead. When checking which actor was restarted please make sure to compare
	 * references by corresponding paths.
	 *
	 * @param ref the reference to terminated actor
	 */
	void onActorTerminated(final ActorRef ref);
}
