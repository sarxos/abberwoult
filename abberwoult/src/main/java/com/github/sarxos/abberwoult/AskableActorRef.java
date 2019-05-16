package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;


/**
 * This is wrapper which hides {@link ActorRef} under the hood and provides nice API to interact
 * with the actor via classic ask pattern.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class AskableActorRef implements Askable {

	private final ActorRef ref;
	private final Timeout timeout;

	public AskableActorRef(final ActorRef ref, final Timeout timeout) {
		this.ref = ref;
		this.timeout = timeout;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> CompletionStage<T> ask(final Object message) {
		return (CompletionStage<T>) PatternsCS.ask(ref, message, timeout);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> CompletionStage<T> ask(final Object message, final Timeout timeout) {
		return (CompletionStage<T>) PatternsCS.ask(ref, message, timeout);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> CompletionStage<T> ask(final Object message, final Duration timeout) {
		return (CompletionStage<T>) PatternsCS.ask(ref, message, timeout);
	}

	@Override
	public void tell(Object message) {
		tell(message, noSender());
	}

	@Override
	public void tell(Object message, ActorRef sender) {
		ref.tell(message, sender);
	}
}
