package com.github.sarxos.abberwoult;

import java.time.Duration;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.config.AskTimeout;


/**
 * A factory class for {@link AskableActorRef} instances.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class AskableActorRefFactory {

	/**
	 * The {@link ActorRef} factory.
	 */
	private final ActorRefFactory factory;

	/**
	 * Ask timeout.
	 */
	private final Duration timeout;

	@Inject
	public AskableActorRefFactory(final ActorRefFactory factory, @AskTimeout Duration timeout) {
		this.factory = factory;
		this.timeout = timeout;
	}

	/**
	 * Create new {@link AskableActorRef}. This method is annotated as {@link Dependent} because it
	 * requires access to {@link InjectionPoint} to infer class of actor to inject.
	 *
	 * @param injection the injection point
	 * @return New {@link AskableActorRef}
	 */
	@Produces
	@Dependent
	@ActorOf
	public AskableActorRef create(final InjectionPoint injection) {
		return new AskableActorRef(factory.create(injection), timeout);
	}
}
