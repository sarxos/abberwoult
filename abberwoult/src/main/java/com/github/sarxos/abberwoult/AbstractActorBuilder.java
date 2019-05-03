package com.github.sarxos.abberwoult;

import static org.apache.commons.lang3.Validate.notNull;

import org.apache.commons.lang3.builder.Builder;

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
public abstract class AbstractActorBuilder<T, S> implements Builder<T> {

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

	/**
	 * The {@link ActorEngine}.
	 */
	private final ActorEngine engine;

	/**
	 * @param engine the {@link ActorEngine} instance
	 */
	public AbstractActorBuilder(final ActorEngine engine) {
		this.engine = notNull(engine);
	}

	/**
	 * @return {@link Propser}
	 */
	protected Propser propser() {
		return engine.propser();
	}

	/**
	 * @return {@link ActorSystem}
	 */
	protected ActorSystem system() {
		return engine.system();
	}

	/**
	 * @return The {@link ActorEngine}
	 */
	protected ActorEngine engine() {
		return engine;
	}
}
