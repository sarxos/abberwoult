package com.github.sarxos.abberwoult;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.trait.Selector;

import akka.actor.ActorSystem;
import akka.actor.Props;


/**
 * Core entity used to create various actor-related stuff.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ActorEngine implements Selector {

	@Inject
	Propser propser;

	@Inject
	ActorSystem system;

	/**
	 * @return A {@link Propser} entity designed to create actor {@link Props}
	 */
	public Propser propser() {
		return propser;
	}

	/**
	 * @return The {@link ActorSystem}
	 */
	@Override
	public ActorSystem system() {
		return system;
	}

	/**
	 * @return New actor builder
	 */
	public ActorBuilder<?> actor() {
		return new ActorBuilder<>(propser, system);
	}
}
