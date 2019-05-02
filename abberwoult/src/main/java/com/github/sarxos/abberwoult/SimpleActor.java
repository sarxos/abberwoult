package com.github.sarxos.abberwoult;

import static java.lang.invoke.MethodHandles.lookup;

import java.lang.invoke.MethodHandles.Lookup;

import javax.enterprise.inject.spi.CDI;

import akka.actor.AbstractActor;


public abstract class SimpleActor extends AbstractActor {

	@Override
	public Receive createReceive() {

		final Lookup caller = lookup().in(getClass());
		final MessageHandlerRegistry mhr = CDI.current()
			.select(MessageHandlerRegistry.class)
			.get();

		return mhr.newReceive(this, caller, this::unhandled);
	}
}
