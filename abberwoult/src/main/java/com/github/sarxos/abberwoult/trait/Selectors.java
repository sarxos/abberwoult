package com.github.sarxos.abberwoult.trait;

import static com.github.sarxos.abberwoult.util.ActorUtils.getActorPath;

import akka.actor.Actor;
import akka.actor.ActorPath;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;


public interface Selectors {

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
		return select(getActorPath(clazz));
	}

	ActorSystem system();
}
