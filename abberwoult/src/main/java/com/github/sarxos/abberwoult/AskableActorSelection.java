package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.AskableActorUtils.throwIfThrowable;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.Patterns;


/**
 * This is wrapper which hides {@link ActorSelection} under the hood and provides nice API to
 * interact with the actor via classic ask pattern.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class AskableActorSelection implements Askable<Object> {

	private final ActorSelection selection;
	private final Duration timeout;

	public AskableActorSelection(final ActorSelection selection, final Duration timeout) {
		this.selection = selection;
		this.timeout = timeout;
	}

	@Override
	public <T> CompletionStage<T> ask(final Object message, final Duration timeout) {
		return throwIfThrowable(Patterns.ask(selection, message, timeout));
	}

	@Override
	public void tell(Object message, ActorRef sender) {
		selection.tell(message, sender);
	}

	@Override
	public Duration getTimeout() {
		return timeout;
	}
}
