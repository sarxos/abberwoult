package com.github.sarxos.abberwoult.builder;

import static java.util.Objects.requireNonNull;

import com.github.sarxos.abberwoult.Propser;

import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.actor.Props;
import io.vavr.control.Option;


/**
 * @param <T> the type that has to be build
 * @param <S> the self type
 *
 * @author Bartosz Firyn (sarxos)
 */
public abstract class Builder<T, S> {

	/**
	 * {@link ActorRef} creator used in {@link ActorBuilder} to create actor reference from provided
	 * {@link ActorRefFactory}, {@link Props} and optional name.
	 *
	 * @author Bartosz Firyn (sarxos)
	 */
	@FunctionalInterface
	public interface ActorBuilderRefCreator {

		/**
		 * Create new {@link ActorRef} by consuming {@link ActorRefFactory}, {@link Props} for actor
		 * and optional name.
		 *
		 * @param factory the factory which creates {@link ActorRef}
		 * @param props the {@link Props} to be consumed by actor
		 * @param name the optional actor name (there can be only one actor with a given name)
		 * @return Newly created {@link ActorRef}
		 */
		ActorRef create(final ActorRefFactory factory, final Props props, final Option<String> name);
	}

	private final Propser propser;
	private final ActorSystem system;

	/**
	 * @param propser the {@link Propser}
	 * @param system the {@link ActorSystem}
	 */
	public Builder(final Propser propser, final ActorSystem system) {
		this.propser = requireNonNull(propser, "Props factory must not be null!");
		this.system = requireNonNull(system, "Actor system must not be null!");
	}

	/**
	 * @return {@link Propser}
	 */
	protected Propser propser() {
		return propser;
	}

	/**
	 * @return {@link ActorSystem}
	 */
	protected ActorSystem system() {
		return system;
	}

	/**
	 * @return New {@link ActorRef}
	 */
	abstract T create();
}
