package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.AskableActorUtils.throwIfThrowable;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.PostStop;

import akka.actor.ActorRef;
import akka.pattern.Patterns;


/**
 * This is wrapper which hides {@link ActorRef} under the hood and provides nice API to interact
 * with the actor via classic ask pattern. This class can be injected into the actor or application
 * context by using CDI {@link Inject} with {@link ActorOf} qualifier. The scope of instance created
 * in such a way is {@link Dependent} which means that there is new actor created each time this
 * class is injected. The actor created under the hood is supervised by the global "user" guardian
 * and there is no way to make it a child of actor inside which it was injected. Therefore special
 * care need to be taken in order not to leak such actors when beans or other actors inside which
 * this one was injected, are destroyed. If this class is injected inside the actor then it should
 * be disposed in this actor in the method annotated with {@link PostStop} annotation. If this class
 * is injected in the application context, e.g. a bean, then it should be disposed in the
 * {@link PreDestroy} annotated method of this bean.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class AskableActorRef implements Askable<Object> {

	private final ActorRef ref;
	private final Duration timeout;

	public AskableActorRef(final ActorRef ref, final Duration timeout) {
		this.ref = ref;
		this.timeout = timeout;
	}

	@Override
	public <T> CompletionStage<T> ask(final Object message, final Duration timeout) {
		return throwIfThrowable(Patterns.ask(ref, message, timeout));
	}

	@Override
	public void tell(final Object message, final ActorRef sender) {
		ref.tell(message, sender);
	}

	@Override
	public Duration getTimeout() {
		return timeout;
	}
}
