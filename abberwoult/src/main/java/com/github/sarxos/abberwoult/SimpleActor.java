package com.github.sarxos.abberwoult;

import static java.lang.invoke.MethodHandles.lookup;

import javax.enterprise.inject.spi.CDI;

import akka.actor.AbstractActor;


public abstract class SimpleActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return CDI.current()
			.select(MessageHandlerRegistry.class).get()
			.newReceive(this, lookup().in(getClass()));
	}
}
