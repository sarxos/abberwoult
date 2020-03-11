package com.github.sarxos.abberwoult;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;


public class EmptyAbstractActor extends AbstractActor {

	@Override
	public Receive createReceive() {
		return ReceiveBuilder.create()
			.matchAny(m -> System.out.println("Message received: " + m))
			.build();
	}
}
