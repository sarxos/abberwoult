package com.github.sarxos.abberwoult.builder;

import static com.github.sarxos.abberwoult.util.ActorUtils.getActorName;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import io.vavr.control.Option;


public class SingletonBuilder<S extends SingletonBuilder<S>> {

	private final ActorUniverse universe;
	private final ClusterSingletonManagerSettings settings;

	private ActorBuilderRefCreator creator = (factory, props, name) -> name
		.map(n -> factory.actorOf(props, n))
		.getOrElse(() -> factory.actorOf(props));

	private Option<String> role = Option.none();
	private Option<String> name = Option.none();
	private String dispatcher = ActorUtils.DEFAULT_MESSAGE_DISPATCHER_ID;
	private String mailbox = ActorUtils.DEFAULT_MAILBOX_ID;
	private Option<ActorRefFactory> parent = Option.none();

	public SingletonBuilder(final ActorUniverse universe) {
		this.universe = requireNonNull(universe, "Actor universe must not be null!");
		this.settings = ClusterSingletonManagerSettings.create(universe.system());
	}

	private SingletonBuilder() {
		this.universe = null;
		this.settings = null;
	}

	@SuppressWarnings("unchecked")
	public S withName(final String value) {
		name = Option.of(value);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withRole(final String value) {
		role = Option.of(value);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withDispatcher(final String dispatcher) {
		this.dispatcher = requireNonNull(dispatcher, "Dispatcher must not be null!");
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withParent(final ActorRefFactory parent) {
		this.parent = Option.of(parent);
		return (S) this;
	}

	@SuppressWarnings("unchecked")
	public S withActorRefCreator(final ActorBuilderRefCreator creator) {
		this.creator = requireNonNull(creator, "Actor ref creator must not be null");
		return (S) this;
	}

	// internals

	private ActorRefFactory parent() {
		return parent.getOrElse(universe.system());
	}

	// factories

	public ClassBasedSingletonBuilder of(final Class<? extends Actor> clazz) {
		return new ClassBasedSingletonBuilder(clazz);
	}

	public final class ClassBasedSingletonBuilder extends SingletonBuilder<ClassBasedSingletonBuilder> implements Builder {

		private final Class<? extends Actor> clazz;
		private Option<Object[]> args = Option.none();
		private Object terminator = PoisonPill.getInstance();

		ClassBasedSingletonBuilder(final Class<? extends Actor> clazz) {
			this.clazz = clazz;
		}

		public SingletonBuilder<S>.ClassBasedSingletonBuilder withArguments(final Object... args) {
			this.args = Option.of(args);
			return this;
		}

		public SingletonBuilder<S>.ClassBasedSingletonBuilder withTerminationMessage(final Object terminator) {
			this.terminator = requireNonNull(terminator, "Termination message must not be null!");
			return this;
		}

		private Object[] args() {
			return args.getOrElse(EMPTY_OBJECT_ARRAY);
		}

		private Props props() {

			final ClusterSingletonManagerSettings settings = settings();

			final Props props = universe
				.props(clazz, args())
				.withDispatcher(dispatcher)
				.withMailbox(mailbox);

			final Props props2 = ClusterSingletonManager.props(props, terminator, settings);

			return props2;
		}

		private Option<String> name() {
			return name.orElse(() -> getActorName(clazz));
		}

		private ClusterSingletonManagerSettings settings() {
			// other singleton-related settings may be added here
			return Option.of(settings)
				.map(s -> role.map(s::withRole).getOrElse(s))
				.getOrElseThrow(() -> new IllegalStateException("Oh no! Null settings!"));
		}

		@Override
		public ActorRef create() {
			return creator.create(parent(), props(), name());
		}
	}

}
