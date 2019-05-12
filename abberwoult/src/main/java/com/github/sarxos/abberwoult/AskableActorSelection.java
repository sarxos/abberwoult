package com.github.sarxos.abberwoult;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.PatternsCS;
import akka.util.Timeout;


/**
 * This is interface which hides {@link ActorRef} or {@link ActorSelection} under the hood and
 * provides nice API to interact with the actor.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class AskableActorSelection implements Askable {

	private final ActorSelection selection;
	private final Supplier<Timeout> timeout;

	public AskableActorSelection(final ActorSelection selection, final Supplier<Timeout> timeout) {
		this.selection = selection;
		this.timeout = timeout;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> CompletionStage<T> ask(final Object message) {
		return (CompletionStage<T>) PatternsCS.ask(selection, message, timeout.get());
	}
}
