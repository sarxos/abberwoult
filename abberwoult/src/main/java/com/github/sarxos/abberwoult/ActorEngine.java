package com.github.sarxos.abberwoult;

import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;


/**
 * Temporary name. Need to come up with something meaningful.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ActorEngine {

	@Inject
	Propser propser;

	@Inject
	ActorSystem system;

	public Propser getPropser() {
		return propser;
	}

	public ActorSystem getSystem() {
		return system;
	}
}
