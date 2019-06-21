package com.github.sarxos.abberwoult.trait;

import com.github.sarxos.abberwoult.ActorSystemUniverse;

import akka.actor.ActorContext;
import akka.actor.ActorRef;


interface ActorInternal {

	ActorRef self();

	ActorRef sender();

	ActorContext getContext();

	ActorSystemUniverse getUniverse();
}
