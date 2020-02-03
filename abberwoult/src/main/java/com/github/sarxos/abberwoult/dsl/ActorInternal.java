package com.github.sarxos.abberwoult.dsl;

import com.github.sarxos.abberwoult.ActorUniverse;

import akka.actor.AbstractActor.ActorContext;
import akka.actor.ActorRef;


interface ActorInternal {

	/**
	 * @return This actor reference
	 */
	ActorRef self();

	/**
	 * @return The sender actor reference.
	 */
	ActorRef sender();

	ActorContext getContext();

	ActorUniverse getUniverse();
}
