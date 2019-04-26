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
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.util.CollectorUtils;

import akka.actor.AbstractActor.Receive;
import akka.japi.pf.FI.UnitApply;
import akka.japi.pf.ReceiveBuilder;
import io.vavr.control.Option;
import io.vavr.control.Try;


@Singleton
public class MessageHandlerRegistry {

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	private static final Map<Class<?>, Map<Class<?>, MessageHandlerEntry>> REGISTRY = new HashMap<>();

	@Inject
	Validator validator;

	public static void store(
		final Class<?> recipientClass,
		final String handlerName,
		final Class<?> handlerType,
		final List<Class<?>> parameterTypes,
		final Set<Short> validablePositionsSet,
		final Set<Short> assistedPositionsSet) {

		final int parametersCount = parameterTypes.size();
		final List<ParameterEntry> parameters = new ArrayList<>();

		// we work with short because java method cannot have more parameters

		for (short i = 0; i < parametersCount; i++) {

			final Class<?> type = parameterTypes.get(i);
			final boolean isValidable = validablePositionsSet.contains(i);
			final boolean isAssisted = assistedPositionsSet.contains(i);

			parameters.add(new ParameterEntry(type, i, isValidable, isAssisted));
		}

		final Class<?> messageClass;

		if (parameters.size() == 1) {
			messageClass = parameters.get(0).type;
		} else {
			messageClass = parameters.stream()
				.filter(ParameterEntry::isAssisted)
				.map(ParameterEntry::getType)
				.findAny()
				.get();
		}

		System.out.println("Register " + recipientClass + " " + handlerName + " for " + messageClass);

		REGISTRY
			.computeIfAbsent(recipientClass, $ -> new HashMap<>())
			.computeIfAbsent(messageClass, $ -> new MessageHandlerEntry(handlerName, recipientClass, handlerType, parameters));
	}

	public Receive newReceive(final Lookup caller, Consumer<Object> unhandled) {
		System.out.println("New receive for " + caller);
		return Option
			.of(REGISTRY.get(caller.lookupClass()))
			.map(handlers -> createNewReceive(handlers, caller).get())
			.getOrElse(() -> createEmptyReceive(unhandled));
	}

	public Try<Receive> createNewReceive(Map<Class<?>, MessageHandlerEntry> handlers, final Lookup caller) {
		return Try.of(() -> createNewReceive0(handlers, caller));
	}

	public Receive createNewReceive0(Map<Class<?>, MessageHandlerEntry> handlers, final Lookup caller) throws NoSuchMethodException, IllegalAccessException {

		System.out.println("Create new receive for " + handlers);

		final ReceiveBuilder builder = ReceiveBuilder.create();

		for (Entry<Class<?>, MessageHandlerEntry> entry : handlers.entrySet()) {

			final Class<?> messageClass = entry.getKey();
			final MessageHandlerEntry method = entry.getValue();
			final MethodHandle handle = caller.findVirtual(method.declartingClass, method.name, method.methodType);

			System.out.println("Match " + messageClass + " to " + handle);

			builder.match(messageClass, consume(handle, method));
		}

		return builder.build();
	}

	private <T> UnitApply<T> consume(final MethodHandle handle, final MessageHandlerEntry method) {
		return message -> {

			if (method.validable) {
				validator.validate(message);
			}

			System.out.println("Consuming " + message);

			try {
				handle.invokeExact(message);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		};
	}

	public Receive createEmptyReceive(final Consumer<Object> unhandled) {
		System.out.println("Cresting empty receive");
		return ReceiveBuilder.create()
			.matchAny(any -> unhandled.accept(any))
			.build();
	}

	private static class MessageHandlerEntry {

		private final String name;
		private final Class<?> declartingClass;
		private final Class<?> returnedType;
		private final Class<?>[] parameterTypes;
		private final List<ParameterEntry> parameters;
		private final MethodType methodType;
		private final boolean validable;

		public MessageHandlerEntry(final String name, final Class<?> declartingClass, final Class<?> returnedType, List<ParameterEntry> parameters) {
			this.name = name;
			this.declartingClass = declartingClass;
			this.returnedType = returnedType;
			this.parameters = parameters;
			this.parameterTypes = getParameterTypes(parameters);
			this.methodType = MethodType.methodType(returnedType, parameterTypes);
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
