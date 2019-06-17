package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.util.ActorUtils.durationOf;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.ActorSelection;
import akka.util.Timeout;


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

	@ConfigProperty(name = "akka.default-timeout", defaultValue = ActorUtils.DEFAULT_TIMEOUT_SECONDS)
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
		return new AskableActorSelection(factory.create(injection), durationOf(timeout));
	}
}
