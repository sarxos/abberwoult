package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.util.AskableUtils.throwIfThrowable;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.pattern.PatternsCS;
import akka.util.Timeout;


/**
 * This is wrapper which hides {@link ActorSelection} under the hood and provides nice API to
 * interact with the actor via classic ask pattern.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class AskableActorSelection implements Askable {

	private final ActorSelection selection;
	private final Timeout timeout;

	public AskableActorSelection(final ActorSelection selection, final Timeout timeout) {
		this.selection = selection;
		this.timeout = timeout;
	}

	@Override
	public <T> CompletionStage<T> ask(final Object message) {
		return throwIfThrowable(PatternsCS.ask(selection, message, timeout));
	}

	@Override
	public <T> CompletionStage<T> ask(final Object message, final Timeout timeout) {
		return throwIfThrowable(PatternsCS.ask(selection, message, timeout));
	}

	@Override
	public <T> CompletionStage<T> ask(final Object message, final Duration timeout) {
		return throwIfThrowable(PatternsCS.ask(selection, message, timeout));
	}

	@Override
	public void tell(Object message, ActorRef sender) {
		selection.tell(message, sender);
	}

}
