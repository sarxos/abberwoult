package com.github.sarxos.abberwoult;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.util.Timeout;


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
	 * Timeout used for asking actors.
	 */
	@ConfigProperty(name = "akka.default-timeout", defaultValue = ActorUtils.DEFAULT_TIMEOUT_SECONDS)
	Timeout timeout;

	@Inject
	public AskableActorRefFactory(final ActorRefFactory factory) {
		this.factory = factory;
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
