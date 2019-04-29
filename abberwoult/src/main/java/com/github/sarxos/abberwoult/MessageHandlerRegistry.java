package com.github.sarxos.abberwoult;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.xml.bind.ValidationException;

import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.util.CollectorUtils;

import akka.actor.AbstractActor.Receive;
import akka.actor.Actor;
import akka.japi.pf.FI.UnitApply;
import akka.japi.pf.ReceiveBuilder;
import io.vavr.control.Option;


@Singleton
public class MessageHandlerRegistry {

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	/**
	 * A static map where all {@link MessageHandler} points are stored. This map is populated by a
	 * {@link MessageHandlerRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<Class<?>, Map<Class<?>, MessageHandlerMethod>> REGISTRY = new HashMap<>();

	/**
	 * A validator instance used to validate messages when {@link MessageHandler} method contains
	 * argument annotated with {@link Valid} annotation.
	 */
	@Inject
	Validator validator;

	protected static void store(
		final Class<?> recipientClass,
		final String handlerName,
		final Class<?> handlerType,
		final List<Class<?>> parameterTypes,
		final Set<Short> parametersToValidate,
		final Set<Short> parametersToAssist) {

		final int parametersCount = parameterTypes.size();
		final List<ParameterEntry> parameters = new ArrayList<>(parametersCount);

		// we work with short because java method cannot have more parameters

		for (short i = 0; i < parametersCount; i++) {

			final Class<?> type = parameterTypes.get(i);
			final boolean isValidable = parametersToValidate.contains(i);
			final boolean isAssisted = parametersToAssist.contains(i);

			parameters.add(new ParameterEntry(type, i, isValidable, isAssisted));
		}

		final Class<?> messageClass = getMessageClass(parameters);

		REGISTRY
			.computeIfAbsent(recipientClass, $ -> new HashMap<>())
			.computeIfAbsent(messageClass, $ -> new MessageHandlerMethod(handlerName, recipientClass, handlerType, parameters));
	}

	/**
	 * Infer message class from the parameters list.
	 *
	 * @param parameters the parameters list
	 * @return Message class
	 */
	private static Class<?> getMessageClass(final List<ParameterEntry> parameters) {
		if (parameters.size() == 1) {
			return parameters.get(0).type;
		} else {
			return parameters.stream()
				.filter(ParameterEntry::isAssisted)
				.map(ParameterEntry::getType)
				.findAny()
				.get();
		}
	}

	/**
	 * Create new {@link Receive} for a given actor.
	 *
	 * @param actor the actor to create {@link Receive} for
	 * @param caller the {@link Lookup} instance (must be created by actor)
	 * @return New actor initial {@link Receive} behavior
	 */
	public Receive newReceive(final Actor actor, final Lookup caller) {
		return Option
			.of(REGISTRY.get(caller.lookupClass()))
			.map(handlers -> createNewReceive(actor, caller, handlers))
			.getOrElse(() -> createEmptyReceive(actor));
	}

	private Receive createNewReceive(final Actor actor, final Lookup caller, Map<Class<?>, MessageHandlerMethod> handlers) {

		final ReceiveBuilder builder = ReceiveBuilder.create();

		for (Entry<Class<?>, MessageHandlerMethod> entry : handlers.entrySet()) {

			final Class<?> messageClass = entry.getKey();
			final MessageHandlerMethod method = entry.getValue();
			final MethodHandle handle = findVirtualMethod(caller, method);

			if (method.validable) {
				builder.match(messageClass, consumeValid(actor, handle));
			} else {
				builder.match(messageClass, consume(actor, handle));
			}
		}

		return builder.build();
	}

	private MethodHandle findVirtualMethod(final Lookup caller, final MessageHandlerMethod method) {

		final Class<?> declaringClass = method.declaringClass;
		final String name = method.name;
		final MethodType methodType = method.type;

		try {
			return caller.findVirtual(declaringClass, name, methodType);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private <T> UnitApply<T> consumeValid(final Actor actor, final MethodHandle handle) {
		return message -> {

			final Set<ConstraintViolation<T>> violations = validator.validate(message);
			if (!violations.isEmpty()) {
				// XXX think about some nicer exception message here
				throw new ValidationException("Message validation exception: " + violations);
			}

			try {
				handle.invoke(actor, message);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		};
	}

	private <T> UnitApply<T> consume(final Actor actor, final MethodHandle handle) {
		return message -> {
			try {
				handle.invoke(actor, message);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		};
	}

	private Receive createEmptyReceive(final Actor actor) {
		return ReceiveBuilder.create()
			.matchAny(actor::unhandled)
			.build();
	}

	private static class MessageHandlerMethod {

		private final String name;
		private final Class<?> declaringClass;
		private final Class<?> returnedType;
		private final Class<?>[] parameterTypes;
		private final List<ParameterEntry> parameters;
		private final MethodType type;
		private final boolean validable;

		public MessageHandlerMethod(final String name, final Class<?> declartingClass, final Class<?> returnedType, final List<ParameterEntry> parameters) {
			this.name = name;
			this.declaringClass = declartingClass;
			this.returnedType = returnedType;
			this.parameters = parameters;
			this.parameterTypes = getParameterTypes(parameters);
			this.type = MethodType.methodType(returnedType, parameterTypes);
			this.validable = hasValidableParameters(parameters);
		}

		private static final Class<?>[] getParameterTypes(final List<ParameterEntry> parameters) {
			return parameters.stream()
				.map(ParameterEntry::getType)
				.collect(CollectorUtils.toListWithSameSizeAs(parameters))
				.toArray(EMPTY_CLASS_ARRAY);
		}

		private static final boolean hasValidableParameters(final List<ParameterEntry> parameters) {
			return parameters.stream()
				.filter(ParameterEntry::isValidable)
				.findAny()
				.isPresent();
		}
	}

	private static class ParameterEntry {

		private final Class<?> type;
		private final short position;
		private final boolean validable;
		private final boolean assisted;

		public ParameterEntry(Class<?> type, short position, boolean validable, boolean assisted) {
			this.type = type;
			this.position = position;
			this.validable = validable;
			this.assisted = assisted;
		}

		public Class<?> getType() {
			return type;
		}

		public boolean isValidable() {
			return validable;
		}

		public boolean isAssisted() {
			return assisted;
		}
	}
}
