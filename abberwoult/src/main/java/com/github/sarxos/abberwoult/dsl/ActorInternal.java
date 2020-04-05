package com.github.sarxos.abberwoult.dsl;

import akka.actor.AbstractActor.ActorContext;
import akka.actor.ActorRef;


interface ActorInternal extends Universe {

	/**
	 * @return This actor reference
	 */
	ActorRef self();

	/**
	 * @return The sender actor reference.
	 */
	ActorRef sender();

	ActorContext getContext();

}
