package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.AskableActorUtils.throwIfThrowable;

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
	public <T> CompletionStage<T> ask(final Object message) {
		return throwIfThrowable(PatternsCS.ask(ref, message, timeout));
	}

	@Override
	public <T> CompletionStage<T> ask(final Object message, final Timeout timeout) {
		return throwIfThrowable(PatternsCS.ask(ref, message, timeout));
	}

	@Override
	public <T> CompletionStage<T> ask(final Object message, final Duration timeout) {
		return throwIfThrowable(PatternsCS.ask(ref, message, timeout));
	}

	@Override
	public void tell(final Object message, final ActorRef sender) {
		ref.tell(message, sender);
	}
}
