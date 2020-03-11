package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getObservedEventsFor;
import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getReceiversFor;
import static com.github.sarxos.abberwoult.deployment.ActorLifecycleRegistry.getPostStopsFor;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.builder.ActorBuilder;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.MessageReceiverMethod;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.PostStopMethod;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.PreStartMethod;
import com.github.sarxos.abberwoult.exception.MessageHandlerInvocationException;
import com.github.sarxos.abberwoult.exception.MessageHandlerValidationException;
import com.github.sarxos.abberwoult.exception.PostStopInvocationException;
import com.github.sarxos.abberwoult.exception.PreStartInvocationException;

import akka.actor.AbstractActor;
import akka.actor.PoisonPill;
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

	/**
	 * Invoke all {@link PreStart} bindings. This methods is final because we do not want anyone to
	 * override it. If someone override it then {@link PreStart} bindings will not work in such an
	 * actor. It's important to note that {@link PreStart} bindings are invoked only after actor
	 * instance is created and all injection points are wired. Is called when an actor is started.
	 * Actors are automatically started asynchronously after they are created. There is no need to
	 * start it manually.
	 *
	 * @see akka.actor.AbstractActor#preStart()
	 */
	// must not be final
	@Override
	public void preStart() throws Exception {
		final Class<?> clazz = getClass();
		getObservedEventsFor(clazz).forEach(this::subscribeEvent);
	}

	/**
	 * Invoke all {@link PostStop} bindings. This methods is final because we do not want anyone to
	 * override it. If someone override it then {@link PostStop} bindings will not work in such
	 * actor. Is called when an actor context is stopped, actor is killed with {@link PoisonPill} or
	 * when it dies due to exception being thrown from the message processing.
	 *
	 * @see akka.actor.AbstractActor#postStop()
	 */
	// final, we do not want anyone to override it (use annotation binding instead)
	@Override
	public final void postStop() throws Exception {
		getPostStopsFor(getClass()).forEach(this::invokePostStop);
	}

	// final, we do not want anyone to override it
	@Override
	public final Receive createReceive() {
		return createReceiveAutomation();
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

	/**
	 * Gracefully disposes this actor by sending {@link PoisonPill} to {@link #self()}. If any
	 * message was enqueued before invoking this method, it will be processed. This method can be
	 * override in a more specialized actors.
	 */
	public void dispose() {
		getSelf().tell(PoisonPill.getInstance(), getSelf());
	}

	// internal stuff

	private void invokePreStart(final PreStartMethod method) {
		final MethodHandle handle = method.getHandle();
		try {
			handle.invoke(this);
		} catch (Throwable e) {
			throw new PreStartInvocationException(this, handle, e);
		}
	}

	private void invokePostStop(final PostStopMethod method) {
		final MethodHandle handle = method.getHandle();
		try {
			handle.invoke(this);
		} catch (Throwable e) {
			throw new PostStopInvocationException(this, handle, e);
		}
	}

	private void subscribeEvent(final Class<?> eventClass) {
		universe.subscribeEvent(self(), eventClass);
	}

	/**
	 * Create {@link Receive} instance constructed form {@link Receives}-annotated methods recorded
	 * in the {@link ActorInterceptorRegistry}.
	 *
	 * @return Automated {@link Receive} instance
	 */
	private Receive createReceiveAutomation() {
		final ReceiveBuilder builder = ReceiveBuilder.create();
		return getReceiversFor(getClass())
			.map(handlers -> createReceiveForReceivers(builder, handlers))
			.getOrElse(() -> createReceiveForUnmatched(builder));
	}

	/**
	 * Create new {@link Receive} for {@link Receives} annotated methods.
	 *
	 * @param builder the {@link ReceiveBuilder} used to create {@link Receive}
	 * @param receivers the mapping between message class and corresponding {@link Receives}
	 * @return New {@link Receive} created from receiver methods
	 */
	private Receive createReceiveForReceivers(final ReceiveBuilder builder, final Map<String, MessageReceiverMethod> receivers) {

		for (final MessageReceiverMethod method : receivers.values()) {

			final Class<?> messageClass = method.getMessageClass();
			final MethodHandle handle = method.getHandle();

			if (method.isValidable()) {
				builder.match(messageClass, consumeValid(handle)::accept);
			} else {
				builder.match(messageClass, consume(handle)::accept);
			}
		}

		return builder
			.matchAny(this::unhandled)
			.build();
	}

	/**
	 * @return New {@link Receive} which invokes unmatched
	 */
	private Receive createReceiveForUnmatched(final ReceiveBuilder builder) {
		return builder
			.matchAny(this::unhandled)
			.build();
	}

	/**
	 * Create consumer which validates a message before it's received.
	 *
	 * @param <T> the generic message type
	 * @param handle the {@link MethodHandle} to invoke
	 * @return The message {@link Consumer}
	 */
	private <T> Consumer<T> consumeValid(final MethodHandle handle) {
		return message -> invoke(handle, validate(message));
	}

	/**
	 * Create consumer which receives a message.
	 *
	 * @param <T> the generic message type
	 * @param handle the {@link MethodHandle} to invoke
	 * @return The message {@link Consumer}
	 */
	private <T> Consumer<T> consume(final MethodHandle handle) {
		return message -> invoke(handle, message);
	}

	/**
	 * Invoke a method which corresponds to the provided {@link Receives} using this object as a
	 * context and a message object as an argument.
	 *
	 * @param handle the {@link MethodHandle} to invoke
	 * @param message the message object to be passed as the argument
	 */
	private void invoke(final MethodHandle handle, final Object message) {
		try {
			handle.invoke(this, message);
		} catch (Throwable e) {
			throw new MessageHandlerInvocationException(this, handle, e);
		}
	}

	/**
	 * Validate input message.
	 *
	 * @param <T> the generic message type
	 * @param message the message to validate
	 * @return Same message but valid
	 * @throws MessageHandlerValidationException if message is invalid
	 */
	private <T> T validate(final T message) {

		final Validator validator = universe.validator();
		final Set<ConstraintViolation<T>> violations = validator.validate(message);

		if (violations.isEmpty()) {
			return message;
		}

		throw new MessageHandlerValidationException(violations);
	}

	public ActorUniverse getUniverse() {
		return universe;
	}
}
