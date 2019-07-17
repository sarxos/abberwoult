package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import akka.actor.ActorRef;


public interface Tellable<M> {

	/**
	 * Send message to the underlying actor.
	 *
	 * @param message the message to send
	 */
	default void tell(final M message) {
		tell(message, noSender());
	}

	/**
	 * Send message to the underlying actor. The response will be send to given sender.
	 *
	 * @param message the message to send
	 * @param sender the sender actor should reply to
	 */
	void tell(final M message, final ActorRef sender);
}
