package com.github.sarxos.abberwoult.trait.internal;

import akka.actor.ActorContext;
import akka.actor.ActorRef;


public interface ActorContextAccess {

	ActorRef self();

	ActorRef sender();

	ActorContext context();
}
