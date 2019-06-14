package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getObservedEventsFor;
import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getPostStopsFor;
import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getPreStartsFor;
import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getReceiversFor;

import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;

import com.github.sarxos.abberwoult.annotation.Receives;
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


public class SimpleActor extends AbstractActor {

	/**
	 * The actor system universe used for a bunch of things.
	 */
	@Inject
	private ActorSystemUniverse universe;

	// final, we do not want anyone to override it
	@Override
	public final void preStart() throws Exception {
		final Class<?> clazz = getClass();
		getPreStartsFor(clazz).forEach(this::invokePreStart);
		getObservedEventsFor(clazz).forEach(this::subscribeEvent);
	}

	// final, we do not want anyone to override it
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
	 * @return Automated {@link Receive} constructed form {@link Receives} methods
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

		final Set<ConstraintViolation<T>> violations = universe.validator().validate(message);

		if (violations.isEmpty()) {
			return message;
		}

		throw new MessageHandlerValidationException(violations);
	}
}
