package com.github.sarxos.abberwoult.cdi;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.event.EventStream;


@Singleton
public class EventStreamFactory {

	private final ActorSystem system;

	@Inject
	public EventStreamFactory(ActorSystem system) {
		this.system = system;
	}

	@Produces
	public EventStream create(final InjectionPoint injection) {
		return system.getEventStream();
	}
}
