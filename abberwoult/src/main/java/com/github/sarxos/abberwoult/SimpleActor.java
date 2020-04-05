package com.github.sarxos.abberwoult;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.builder.ActorBuilder;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;


/**
 * This is the father of all injectable and autowired actors. If you want to have actor with
 * injectable fields which cal also be injected into the application context, please extend this
 * one.
 *
 * @author Bartosz Firyn (sarxos)
 */
public abstract class SimpleActor extends AbstractActor {

	/**
	 * The actor system universe used for a bunch of things. This one can be private because it's
	 * not injected by CDI but with the specialized {@link ActorCreator}.
	 */
	@Inject
	private ActorUniverse universe;

	@Override
	public Receive createReceive() {
		return ReceiveBuilder.create()
			.matchAny(message -> unhandled(message))
			.build();
	}

	/**
	 * An utility methods to build new actor which will be a child of this actor.
	 *
	 * @return New universal {@link ActorBuilder} with pre-configured parent
	 */
	public ActorBuilder<?> actor() {
		return universe
			.actor()
			.withParent(getContext());
	}

	public ActorUniverse getUniverse() {
		return universe;
	}
}
