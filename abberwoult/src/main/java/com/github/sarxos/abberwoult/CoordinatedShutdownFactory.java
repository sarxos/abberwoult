package com.github.sarxos.abberwoult;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.actor.CoordinatedShutdown;


/**
 * A factory bean which creates {@link CoordinatedShutdown}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class CoordinatedShutdownFactory {

	private final CoordinatedShutdown shutdown;

	@Inject
	public CoordinatedShutdownFactory(final ActorSystem system) {
		this.shutdown = CoordinatedShutdown.get(system);
	}

	@Produces
	public CoordinatedShutdown create() {
		return shutdown;
	}
}
