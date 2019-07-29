package com.github.sarxos.abberwoult.dsl;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;


public interface Creators extends ActorInternal {

	default ActorRef actorOf(final Class<? extends Actor> clazz, final Object... args) {
		return getUniverse()
			.actor()
			.of(clazz)
			.withParent(getContext())
			.withArguments(args)
			.create();
	}

	default ActorRef actorOf(final Props props) {
		return getUniverse()
			.actor()
			.of(props)
			.withParent(getContext())
			.create();
	}
}
