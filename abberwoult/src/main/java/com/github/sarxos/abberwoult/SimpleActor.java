package com.github.sarxos.abberwoult;

import java.lang.invoke.MethodHandles;

import javax.enterprise.inject.spi.CDI;

import akka.actor.AbstractActor;


public abstract class SimpleActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return CDI.current()
			.select(MessageHandlerRegistry.class).get()
			.newReceive(MethodHandles.lookup(), this::unhandled);
	}
}
