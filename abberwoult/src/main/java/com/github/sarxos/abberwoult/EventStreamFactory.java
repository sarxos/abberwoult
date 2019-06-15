package com.github.sarxos.abberwoult;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.event.EventStream;


/**
 * This is a factory bean to produce {@link EventStream} instance used by {@link ActorSystem}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class EventStreamFactory {

	private final ActorSystem system;

	@Inject
	public EventStreamFactory(final ActorSystem system) {
		this.system = system;
	}

	@Produces
	public EventStream create(final InjectionPoint injection) {
		return system.getEventStream();
	}
}
