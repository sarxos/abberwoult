package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.util.ReflectionUtils.findVirtualMethod;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.MessageHandlersRegistry.MessageHandlerMethod;
import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.exception.MessageHandlerInvocationException;
import com.github.sarxos.abberwoult.exception.MessageHandlerValidationException;

import akka.actor.AbstractActor;
import akka.japi.pf.FI.UnitApply;
import akka.japi.pf.ReceiveBuilder;


public class SimpleActor extends AbstractActor {

	/**
	 * A validator instance used to validate message in case when {@link MessageHandler} annotated
	 * method contains an argument which was annotated with {@link Valid} annotation.
	 */
	@Inject
	Validator validator;

	@Override
	public Receive createReceive() {
		return createReceiveAutomation();
	}

	private MessageHandlersRegistry getMessageHandlersRegistry() {
		return CDI.current()
			.select(MessageHandlersRegistry.class)
			.get();
	}

	/**
	 * @return Automated {@link Receive} constructed form {@link MessageHandler} methods
	 */
	private Receive createReceiveAutomation() {

		final Class<?> declaringClass = getClass();
		final Lookup caller = MethodHandles.lookup().in(declaringClass);

		return getMessageHandlersRegistry()
			.getHandlersFor(declaringClass)
			.map(handlers -> createReceiveForHandlers(caller, handlers))
			.getOrElse(() -> createReceiveForUnmatched());
	}

	/**
	 * Create new {@link Receive} for {@link MessageHandler} annotated methods.
	 *
	 * @param caller a called {@link Lookup}
	 * @param handlers a mapping between message class and corresponding {@link MessageHandler}
	 * @return New {@link Receive}
	 */
	private Receive createReceiveForHandlers(final Lookup caller, final Map<Class<?>, MessageHandlerMethod> handlers) {

		final ReceiveBuilder builder = ReceiveBuilder.create();

		for (final Entry<Class<?>, MessageHandlerMethod> entry : handlers.entrySet()) {

			final Class<?> messageClass = entry.getKey();
			final MessageHandlerMethod method = entry.getValue();
			final MethodHandle handle = findVirtualMethod(caller, method);

			if (method.isValidable()) {
				builder.match(messageClass, consumeValid(handle));
			} else {
				builder.match(messageClass, consume(handle));
			}
		}

		return builder
			.matchAny(this::unhandled)
			.build();
	}

	/**
	 * @return New {@link Receive} which invokes unmatched
	 */
	private Receive createReceiveForUnmatched() {
		return ReceiveBuilder.create()
			.matchAny(this::unhandled)
			.build();
	}

	private <T> UnitApply<T> consumeValid(final MethodHandle handle) {
		return message -> invoke(handle, validate(message));
	}

	private <T> UnitApply<T> consume(final MethodHandle handle) {
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

	private <T> T validate(final T message) {

		final Set<ConstraintViolation<T>> violations = validator.validate(message);
		if (!violations.isEmpty()) {
			throw new MessageHandlerValidationException(violations);
		}

		return message;
	}
}
