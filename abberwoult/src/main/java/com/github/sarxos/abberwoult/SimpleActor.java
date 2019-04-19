package com.github.sarxos.abberwoult;

import akka.actor.AbstractActor;


public abstract class SimpleActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.matchAny(message -> System.out.println(message))
			.build();
	}
}
