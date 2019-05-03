package com.github.sarxos.abberwoult;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_OBJECT_ARRAY;

import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.Actor;
import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.actor.ActorRefFactory;
import akka.actor.ActorSystem;
import akka.actor.Props;
import io.vavr.control.Option;
import io.vavr.control.Try;


/**
 * Actor reference builder.
 *
 * @param <S> self type
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ActorBuilder<S extends ActorBuilder<?>> extends AbstractActorBuilder<ActorRef, S> {

	private Option<Class<? extends Actor>> clazz = Option.none();
	private Option<String> name = Option.none();
	private Option<Props> props = Option.none();
	private Option<String> dispatcher = Option.none();
	private Option<Object[]> args = Option.none();
	private Option<ActorRefFactory> parent = Option.none();

	private ActorBuilderRefCreator creator = (factory, props, name) -> name
		.map(n -> factory.actorOf(props, n))
		.getOrElse(() -> factory.actorOf(props));

	/**
	 * To be used internally. Please do not use this constructor!
	 *
	 * @param engine the {@link ActorEngine} instance
	 */
	protected ActorBuilder(final ActorEngine engine) {
		this(engine, null);
	}

	/**
	 * Copying constructor. To be used internally. Please do not use this constructor ion your code
	 * unless you know what you are doing!
	 *
	 * @param engine the entity which creates various actor-related stuff
	 * @param builder the original builder.
	 */
	protected ActorBuilder(final ActorEngine engine, final ActorBuilder<?> builder) {

		super(engine);

		if (builder != null) {
			this.clazz = builder.clazz;
			this.name = builder.name;
			this.props = builder.props;
			this.args = builder.args;
			this.dispatcher = builder.dispatcher;
		}
	}

	// fluent methods

	/**
	 * Create actor reference of given class
	 *
	 * @param clazz the actor class
	 * @return This {@link ActorBuilder}
	 */
	@SuppressWarnings("unchecked")
	public S of(Class<? extends Actor> clazz) {
		if (props.isDefined()) {
			throw new IllegalStateException("Cannot assign actor class because props are already set");
		}
		this.clazz = Option.of(clazz);
		return (S) this;
	}

	/**
	 * Create actor with a given constructor arguments. After this method is used one cannot use
	 * {@link #withProps(Props)} method on this builder.
	 *
	 * @param args the actor constructor arguments, must not be null
	 * @return This {@link ActorBuilder}
	 * @throws IllegalStateException if {@link #withProps(Props)} has been used
	 */
	@SuppressWarnings("unchecked")
	public S withArguments(Object... args) {
		if (props.isDefined()) {
			throw new IllegalStateException("Cannot assign arguments because props are already set");
		}
		this.args = Option.of(args);
		return (S) this;
	}

	/**
	 * Create actor with a given {@link Props}. After this method is used one cannot use
	 * {@link #withArguments(Object...)} method on this builder.
	 *
	 * @param props the {@link Props}, must not be null
	 * @return This {@link ActorBuilder}
	 * @throws IllegalStateException if {@link #withArguments(Object...)} has been used
	 */
	@SuppressWarnings("unchecked")
	public S withProps(Props props) {
		if (args.isDefined()) {
			throw new IllegalStateException("Cannot assign props because arguments are already set");
		}
		if (clazz.isDefined()) {
			throw new IllegalStateException("Cannot assign props because actor class is already set");
		}
		this.props = Option.of(props);
		return (S) this;
	}

	/**
	 * Create actor with a given name. Name given in the argument must not be null.
	 *
	 * @param name the actor name (must not be null)
	 * @return This {@link ActorBuilder}
	 */
	@SuppressWarnings("unchecked")
	public S withName(String name) {
		this.name = Option.of(name);
		return (S) this;
	}

	/**
	 * Create actor with a given name. Name given in the argument may be null. This method should be
	 * used only internally.
	 *
	 * @param name the actor name (may be null)
	 * @return This {@link ActorBuilder}
	 */
	@SuppressWarnings("unchecked")
	protected S withNameNullable(String name) {
		this.name = Option.of(name);
		return (S) this;
	}

	/**
	 * Create actor which uses given message dispatcher.
	 *
	 * @param dispatcher the dispatcher, must not be null or empty string
	 * @return This {@link ActorBuilder}
	 */
	@SuppressWarnings("unchecked")
	public S withDispatcher(String dispatcher) {
		this.dispatcher = Option.of(dispatcher);
		return (S) this;
	}

	/**
	 * Create actor under another actor (guardian). When parent actor (guardian) dies the newly
	 * created actor dies as well.
	 *
	 * @param parent the parent actor (guardian)
	 * @return This {@link ActorBuilder}
	 */
	@SuppressWarnings("unchecked")
	public S withParent(ActorRefFactory parent) {
		this.parent = Option.of(parent);
		return (S) this;
	}

	/**
	 * Create actor with a different {@link ActorBuilderRefCreator}.
	 *
	 * @param creator the creator, must not be null
	 * @return This builder
	 */
	@SuppressWarnings("unchecked")
	public S withActorRefCreator(ActorBuilderRefCreator creator) {
		if (creator == null) {
			throw new IllegalArgumentException("Actor reference creator must not be null");
		}
		this.creator = creator;
		return (S) this;
	}

	// internals

	/**
	 * @return Actor class
	 * @throws NullPointerException if class has not been defined
	 */
	protected Class<? extends Actor> clazz() {
		return clazz.get();
	}

	/**
	 * @return Actor constructor arguments or empty array if not defined
	 */
	protected Object[] arguments() {
		return args.getOrElse(EMPTY_OBJECT_ARRAY);
	}

	/**
	 * @return Actor props
	 */
	public Props props() {
		Props p = props.getOrElse(() -> propser().props(clazz(), arguments()));
		if (dispatcher.isDefined() || clazz.isDefined()) {
			return p.withDispatcher(dispatcher());
		}
		return p;
	}

	/**
	 * @return Actor name or null if not defined
	 */
	protected Option<String> name() {
		return name.orElse(() -> clazz.flatMap(ActorUtils::getActorName));
	}

	/**
	 * @return Actor message dispatcher or default dispatcher if not defined
	 */
	protected String dispatcher() {
		return dispatcher
			.orElse(() -> clazz.map(ActorUtils::getMessageDispatcherId))
			.getOrElse(ActorUtils.DEFAULT_MESSAGE_DISPATCHER_ID);
	}

	/**
	 * Return actor references factory (either {@link ActorSystem} or {@link ActorContext}).
	 *
	 * @return {@link ActorRefFactory}
	 */
	protected ActorRefFactory factory() {
		return parent.getOrElse(system());
	}

	/**
	 * Create actor reference.
	 *
	 * @param props the actor {@link Props}
	 * @param name the actor name (may be null, in this case name is generated)
	 * @return
	 */
	protected ActorRef create(ActorRefFactory factory, Props props, Option<String> name) {
		return creator.create(factory, props, name);
	}

	@Override
	public ActorRef build() {
		return create(factory(), props(), name());
	}

	public Try<ActorRef> buildTry() {
		return Try.of(this::build);
	}
}
