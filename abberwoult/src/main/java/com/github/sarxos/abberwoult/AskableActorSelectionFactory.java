package com.github.sarxos.abberwoult;

import java.time.Duration;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.config.AskTimeout;

import akka.actor.ActorSelection;


/**
 * A factory bean which creates {@link AskableActorSelection} instances with {@link Dependent}
 * scope.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class AskableActorSelectionFactory {

	/**
	 * The {@link ActorSelection} factory.
	 */
	private final ActorSelectionFactory factory;

	/**
	 * Ask timeout.
	 */
	private final Duration timeout;

	@Inject
	public AskableActorSelectionFactory(final ActorSelectionFactory factory, @AskTimeout Duration timeout) {
		this.factory = factory;
		this.timeout = timeout;
	}

	/**
	 * Create new {@link AskableActorSelection}. This method is annotated as {@link Dependent}
	 * because it requires access to {@link InjectionPoint} to infer class of actor to inject.
	 *
	 * @param injection the injection point
	 * @return New {@link AskableActorSelection}
	 */
	@Produces
	@Dependent
	@ActorOf
	public AskableActorSelection create(final InjectionPoint injection) {
		return new AskableActorSelection(factory.create(injection), timeout);
	}
}
