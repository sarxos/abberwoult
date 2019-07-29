package com.github.sarxos.abberwoult.builder;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.Props;
import io.vavr.control.Option;


@FunctionalInterface
public interface ActorBuilderRefCreator {

	/**
	 * Create new {@link ActorRef} by consuming {@link ActorRefFactory}, {@link Props} for actor and
	 * optional name.
	 *
	 * @param factory the factory which creates {@link ActorRef}
	 * @param props the {@link Props} to be consumed by actor
	 * @param name the optional actor name (there can be only one actor with a given name)
	 * @return Newly created {@link ActorRef}
	 */
	ActorRef create(final ActorRefFactory factory, final Props props, final Option<String> name);
}
