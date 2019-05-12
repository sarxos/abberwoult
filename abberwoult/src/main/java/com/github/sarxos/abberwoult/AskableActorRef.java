package com.github.sarxos.abberwoult;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

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
	private final Supplier<Timeout> timeout;

	public AskableActorRef(final ActorRef ref, final Supplier<Timeout> timeout) {
		this.ref = ref;
		this.timeout = timeout;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> CompletionStage<T> ask(final Object message) {
		return (CompletionStage<T>) PatternsCS.ask(ref, message, timeout.get());
	}
}
