package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.util.ActorUtils.DEFAULT_TIMEOUT_SECONDS;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.sarxos.abberwoult.AskableActorSelection;
import com.github.sarxos.abberwoult.annotation.ActorOf;

import akka.actor.ActorSelection;
import akka.util.Timeout;


/**
 * A factory class for {@link AskableActorSelection} instances.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class AskableActorSelectionFactory {

	/**
	 * The {@link ActorSelection} factory.
	 */
	private final ActorSelectionFactory factory;

	@ConfigProperty(name = "akka.default-timeout", defaultValue = DEFAULT_TIMEOUT_SECONDS)
	Timeout timeout;

	@Inject
	public AskableActorSelectionFactory(final ActorSelectionFactory factory) {
		this.factory = factory;
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
