package com.github.sarxos.abberwoult.cdi;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.Actor;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;


@Singleton
public class ActorSelectionFactory extends AbstractInjectFactory {

	private static final Logger LOG = LoggerFactory.getLogger(ActorSelectionFactory.class);

	private final ActorSystem system;

	@Inject
	public ActorSelectionFactory(final ActorSystem system) {
		this.system = system;
	}

	@Produces
	@Dependent
	@ActorOf
	public ActorSelection create(final InjectionPoint injection) {

		if (injection == null) {
			throw new NoActorClassProvidedException(injection);
		}

		final Class<? extends Actor> clazz = getActorClass(injection);
		final String path = ActorUtils.getActorPath(clazz);

		LOG.debug("Creating actor selection for path {}", path);

		return system.actorSelection(path);
	}
}
