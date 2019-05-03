package com.github.sarxos.abberwoult.cdi;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.Propser;
import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;


@Singleton
public class ActorRefFactory extends AbstractInjectFactory {

	private static final Logger LOG = LoggerFactory.getLogger(ActorRefFactory.class);

	private final Propser propser;
	private final ActorSystem system;

	@Inject
	public ActorRefFactory(final Propser propser, final ActorSystem system) {
		this.propser = propser;
		this.system = system;
	}

	@Produces
	@Dependent
	@ActorOf
	public ActorRef create(final InjectionPoint injection) {

		if (injection == null) {
			throw new NoActorClassProvidedException(injection);
		}

		final Class<? extends Actor> clazz = getActorClass(injection);
		final Props props = propser.props(clazz);

		LOG.debug("Creating actor {} with props {}", clazz, props);

		return ActorUtils
			.getActorName(clazz)
			.map(name -> system.actorOf(props, name))
			.getOrElse(() -> system.actorOf(props));
	}
}
