package com.github.sarxos.abberwoult;

import java.time.Duration;
import java.util.concurrent.CompletionStage;


public interface Askable<M> extends Tellable<M> {

	static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(60);

	/**
	 * Ask underlying actor to reply with value. This results in {@link CompletionStage} which will
	 * timeout if no response is received. Default timeout value is used.
	 *
	 * @param <T> the expected response type
	 * @param message the message to send to actor
	 * @return {@link CompletionStage} to extract response from
	 */
	default <T> CompletionStage<T> ask(final M message) {
		return ask(message, getTimeout());
	}

	/**
	 * Ask underlying actor to reply with value, but provide non-default ask timeout. This results
	 * in {@link CompletionStage} which will timeout if no response is received.
	 *
	 * @param <T> the expected response type
	 * @param message the message to send to actor
	 * @param timeout the ask timeout to use
	 * @return {@link CompletionStage} to extract response from
	 */
	<T> CompletionStage<T> ask(final M message, final Duration timeout);

	Duration getTimeout();
}
