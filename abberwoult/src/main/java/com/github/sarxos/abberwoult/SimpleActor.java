package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.deployment.MessageHandlerRegistry.getMessageHandlersFor;
import static com.github.sarxos.abberwoult.deployment.PostStopRegistry.getPostStopsFor;
import static com.github.sarxos.abberwoult.deployment.PreStartRegistry.getPreStartsFor;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.deployment.MessageHandlerRegistry.MessageHandlerMethod;
import com.github.sarxos.abberwoult.deployment.PostStopRegistry.PostStopMethod;
import com.github.sarxos.abberwoult.deployment.PreStartRegistry.PreStartMethod;
import com.github.sarxos.abberwoult.exception.MessageHandlerInvocationException;
import com.github.sarxos.abberwoult.exception.MessageHandlerValidationException;
import com.github.sarxos.abberwoult.exception.PostStopInvocationException;
import com.github.sarxos.abberwoult.exception.PreStartInvocationException;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;


public class SimpleActor extends AbstractActor {

	/**
	 * A validator instance used to validate message in case when {@link MessageHandler} annotated
	 * method contains an argument which was annotated with {@link Valid} annotation.
	 */
	@Inject
	Validator validator;

	// final, we do not want anyone to override it
	@Override
	public final void preStart() throws Exception {
		getPreStartsFor(getClass()).forEach(this::invokePreStart);
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

	/**
	 * @return Automated {@link Receive} constructed form {@link MessageHandler} methods
	 */
	private Receive createReceiveAutomation() {
		final ReceiveBuilder builder = ReceiveBuilder.create();
		return getMessageHandlersFor(getClass())
			.map(handlers -> createReceiveForHandlers(builder, handlers))
			.getOrElse(() -> createReceiveForUnmatched(builder));
	}

	/**
	 * Create new {@link Receive} for {@link MessageHandler} annotated methods.
	 *
	 * @param caller a called {@link Lookup}
	 * @param handlers a mapping between message class and corresponding {@link MessageHandler}
	 * @return New {@link Receive}
	 */
	private Receive createReceiveForHandlers(final ReceiveBuilder builder, final Map<String, MessageHandlerMethod> handlers) {

		for (final MessageHandlerMethod method : handlers.values()) {

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
	 * This method validates a message before it's consumed.
	 *
	 * @param <T> the generic message type
	 * @param handle the {@link MethodHandle} to invoke
	 * @return The message {@link Consumer}
	 */
	private <T> Consumer<T> consumeValid(final MethodHandle handle) {
		return message -> invoke(handle, validate(message));
	}

	/**
	 * This method consume a message.
	 *
	 * @param <T> the generic message type
	 * @param handle the {@link MethodHandle} to invoke
	 * @return The message {@link Consumer}
	 */
	private <T> Consumer<T> consume(final MethodHandle handle) {
		return message -> invoke(handle, message);
	}

	/**
	 * Invoke a method which corresponds to the provided {@link MessageHandler} using this object as
	 * a context and a message object as an argument.
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

		final Set<ConstraintViolation<T>> violations = validator.validate(message);

		if (violations.isEmpty()) {
			return message;
		}

		throw new MessageHandlerValidationException(violations);
	}
}
