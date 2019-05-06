package com.github.sarxos.abberwoult.trait.internal;

import akka.actor.ActorContext;
import akka.actor.ActorRef;


public interface InActor {

	ActorRef self();

	ActorRef sender();

	ActorContext context();
}
