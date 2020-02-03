package com.github.sarxos.abberwoult.builder;

import static com.github.sarxos.abberwoult.util.ActorUtils.getActorName;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;

import java.util.Objects;

import com.github.sarxos.abberwoult.ActorUniverse;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.Props;
import io.vavr.control.Option;


/**
 * Actor builder.
 *
 * @param <S> self type
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ActorBuilder<S extends ActorBuilder<S>> {

	private final ActorUniverse universe;

	private ActorBuilderRefCreator creator = (factory, props, name) -> name
		.map(n -> factory.actorOf(props, n))
		.getOrElse(() -> factory.actorOf(props));

	private Option<String> name = Option.none();
	private Option<String> dispatcher = Option.none();
	private Option<String> mailbox = Option.none();
	private Option<ActorRefFactory> parent = Option.none();

	public ActorBuilder(final ActorUniverse universe) {
		this.universe = universe;
	}

	private ActorBuilder() {
		this(null);
	}

	@SuppressWarnings("unchecked")
	public S withName(final String value) {
		name = Option.of(value);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withDispatcher(final String value) {
		dispatcher = Option.of(value);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withParent(final ActorRefFactory parent) {
		this.parent = Option.of(parent);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withActorRefCreator(final ActorBuilderRefCreator creator) {
		this.creator = Objects.requireNonNull(creator, "Actor ref creator must not be null");
		return (S) this;
	}

	// factories

	public PropsBasedActorBuilder of(final Props props) {
		return new PropsBasedActorBuilder(props);
	}

	public ClassBasedActorBuilder of(final Class<? extends Actor> clazz) {
		return new ClassBasedActorBuilder(clazz);
	}

	// internals

	private ActorRefFactory parent() {
		return parent.getOrElse(universe.system());
	}

	// impls

	public final class PropsBasedActorBuilder extends ActorBuilder<PropsBasedActorBuilder> implements Builder {

		private final Props props;

		public PropsBasedActorBuilder(final Props props) {
			this.props = props;
		}

		private Props props() {
			return Option.of(props)
				.map(p -> dispatcher.map(p::withDispatcher).getOrElse(p))
				.map(p -> mailbox.map(p::withMailbox).getOrElse(p))
				.get();
		}

		@Override
		public ActorRef create() {
			return creator.create(parent(), props(), name);
		}
	}

	public final class ClassBasedActorBuilder extends ActorBuilder<ClassBasedActorBuilder> implements Builder {

		private Class<? extends Actor> clazz;
		private Option<Object[]> args = Option.none();

		public ClassBasedActorBuilder(final Class<? extends Actor> clazz) {
			this.clazz = requireNonNull(clazz, "Actor class must not be null");
		}

		private Object[] args() {
			return args.getOrElse(EMPTY_OBJECT_ARRAY);
		}

		private Props props() {
			return universe
				.propser()
				.props(clazz, args());
		}

		private Option<String> name() {
			return name.orElse(() -> getActorName(clazz));
		}

		public ActorBuilder<S>.ClassBasedActorBuilder withArguments(final Object... values) {
			args = Option.of(values);
			return this;
		}

		@Override
		public ActorRef create() {
			return creator.create(parent(), props(), name());
		}
	}
}
