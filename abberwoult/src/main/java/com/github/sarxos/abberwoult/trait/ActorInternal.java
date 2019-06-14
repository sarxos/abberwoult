package com.github.sarxos.abberwoult.trait;

import akka.actor.ActorContext;
import akka.actor.ActorRef;


interface ActorInternal {

	ActorRef self();

	ActorRef sender();

	ActorContext getContext();
}
