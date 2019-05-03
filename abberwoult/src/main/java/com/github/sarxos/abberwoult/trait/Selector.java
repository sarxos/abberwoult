package com.github.sarxos.abberwoult.trait;

import com.github.sarxos.abberwoult.trait.internal.ActorSystemAccess;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.Actor;
import akka.actor.ActorPath;
import akka.actor.ActorSelection;


public interface Selector extends ActorSystemAccess {

	/**
	 * Select actor from the underlying actor system.
	 *
	 * @param path the actor path
	 * @return The {@link ActorSelection}
	 */
	default ActorSelection select(final String path) {
		return system().actorSelection(path);
	}

	/**
	 * Select actor from the underlying actor system.
	 *
	 * @param path the actor path
	 * @return The {@link ActorSelection}
	 */
	default ActorSelection select(final ActorPath path) {
		return system().actorSelection(path);
	}

	/**
	 * Select actor from the underlying actor system.
	 *
	 * @param clazz the class of actor to be selected
	 * @return The {@link ActorSelection}
	 */
	default ActorSelection select(final Class<? extends Actor> clazz) {
		return select(ActorUtils.getActorPath(clazz));
	}
}
