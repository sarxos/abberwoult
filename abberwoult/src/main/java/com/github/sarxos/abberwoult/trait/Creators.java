package com.github.sarxos.abberwoult.trait;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;


public interface Creators extends ActorInternal {

	default ActorRef actorOf(final Class<? extends Actor> clazz, final Object... args) {
		return getUniverse()
			.actor()
			.withParent(getContext())
			.of(clazz)
			.withArguments(args)
			.build();
	}

	default ActorRef actorOf(final Props props) {
		return getUniverse()
			.actor()
			.withParent(getContext())
			.withProps(props)
			.build();
	}
}
