package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.util.Timeout;


public interface Askable {

	/**
	 * Ask underlying actor to reply with value. This results in {@link CompletionStage} which will
	 * timeout if no response is received. Default timeout value is used.
	 *
	 * @param <T> the expected response type
	 * @param message the message to send to actor
	 * @return {@link CompletionStage} to extract response from
	 */
	<T> CompletionStage<T> ask(final Object message);

	<T> CompletionStage<T> ask(final Object message, final Timeout timeout);

	<T> CompletionStage<T> ask(final Object message, final Duration timeout);

	/**
	 * Dispose underlying actor.
	 */
	default void dispose() {
		tell(PoisonPill.getInstance());
	}

	/**
	 * Send message to the underlying actor.
	 *
	 * @param message the message to send
	 */
	default void tell(Object message) {
		tell(message, noSender());
	}

	/**
	 * Send message to the underlying actor. The response will be send to given sender.
	 *
	 * @param message the message to send
	 * @param sender the sender actor should reply to
	 */
	void tell(final Object message, final ActorRef sender);
}
