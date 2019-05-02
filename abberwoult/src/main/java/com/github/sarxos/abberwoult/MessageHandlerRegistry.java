package com.github.sarxos.abberwoult;

import static java.util.Collections.unmodifiableMap;

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
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.xml.bind.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.util.CollectorUtils;

import akka.actor.AbstractActor.Receive;
import akka.japi.pf.FI.UnitApply;
import akka.japi.pf.ReceiveBuilder;
import io.vavr.control.Option;


@Singleton
public class MessageHandlerRegistry {

	private static final Logger LOG = LoggerFactory.getLogger(MessageHandlerRegistry.class);

	/**
	 * A static map where all {@link MessageHandler} points are stored. This map is populated by a
	 * {@link MessageHandlerRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<Class<?>, Map<Class<?>, MessageHandlerMethod>> REGISTRY = new HashMap<>();

	/**
	 * A validator instance used to validate message in case when {@link MessageHandler} annotated
	 * method contains an argument which was annotated with {@link Valid} annotation.
	 */
	@Inject
	Validator validator;

	protected static void store(final Class<?> declaringClass, final String handlerName, final Class<?> handlerType, final ParameterList parameters) {
		REGISTRY
			.computeIfAbsent(declaringClass, $ -> new HashMap<>())
			.computeIfAbsent(getMessageClass(parameters), entry(declaringClass, handlerName, handlerType, parameters));
	}

	private static Function<Class<?>, MessageHandlerMethod> entry(final Class<?> declaringClass, final String handlerName, final Class<?> handlerType, final ParameterList parameters) {
		return messageClass -> new MessageHandlerMethod(messageClass, declaringClass, handlerName, handlerType, parameters);
	}

	/**
	 * Infer message class from the parameters list.
	 *
	 * @param parameters the parameters list
	 * @return Message class
	 */
	private static Class<?> getMessageClass(final List<ParameterEntry> parameters) {
		if (parameters.size() == 1) {
			return parameters.get(0).getType();
		} else {
			return parameters.stream()
				.filter(ParameterEntry::isAssisted)
				.map(ParameterEntry::getType)
				.findAny()
				.get();
		}
	}

	public Map<Class<?>, MessageHandlerMethod> getHandlersFor(final Class<?> declaringClass) {
		return unmodifiableMap(REGISTRY.get(declaringClass));
	}

	public MessageHandlerMethod getHandlerFor(final Class<?> declaringClass, final Class<?> messageClass) {
		return REGISTRY
			.get(declaringClass)
			.get(messageClass);
	}

	/**
	 * Create new {@link Receive} for a given actor.
	 *
	 * @param actor the actor to create {@link Receive} for
	 * @param caller the {@link Lookup} instance (must be created by actor)
	 * @return New actor initial {@link Receive} behavior
	 */
	public Receive newReceive(final Object thiz, final Lookup caller, final Consumer<Object> unhandled) {
		return Option
			.of(REGISTRY.get(caller.lookupClass()))
			.map(handlers -> createNewReceive(thiz, caller, handlers, unhandled))
			.getOrElse(() -> createEmptyReceive(unhandled));
	}

	private Receive createNewReceive(final Object thiz, final Lookup caller, final Map<Class<?>, MessageHandlerMethod> handlers, final Consumer<Object> unhandled) {

		LOG.trace("Creating receive for {}", caller);

		final ReceiveBuilder builder = ReceiveBuilder.create();

		for (Entry<Class<?>, MessageHandlerMethod> entry : handlers.entrySet()) {

			final Class<?> messageClass = entry.getKey();
			final MessageHandlerMethod method = entry.getValue();
			final MethodHandle handle = findVirtualMethod(caller, method);

			if (method.isValidable()) {
				builder.match(messageClass, consumeValid(thiz, handle));
			} else {
				builder.match(messageClass, consume(thiz, handle));
			}
		}

		return builder
			.matchAny(unhandled::accept)
			.build();
	}

	private MethodHandle findVirtualMethod(final Lookup caller, final MessageHandlerMethod method) {

		final Class<?> declaringClass = method.getDeclaringClass();
		final String name = method.getName();
		final MethodType methodType = method.getType();

		try {
			return caller.findVirtual(declaringClass, name, methodType);
		} catch (NoSuchMethodException | IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	private <T> UnitApply<T> consumeValid(final Object thiz, final MethodHandle handle) {
		return message -> {

			final Set<ConstraintViolation<T>> violations = validator.validate(message);
			if (!violations.isEmpty()) {
				// XXX think about some nicer exception message here
				throw new ValidationException("Message validation exception: " + violations);
			}

			try {
				handle.invoke(thiz, message);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		};
	}

	private <T> UnitApply<T> consume(final Object thiz, final MethodHandle handle) {
		return message -> {
			try {
				handle.invoke(thiz, message);
			} catch (Throwable e) {
				throw new IllegalStateException(e);
			}
		};
	}

	private Receive createEmptyReceive(final Consumer<Object> unhandled) {
		return ReceiveBuilder.create()
			.matchAny(unhandled::accept)
			.build();
	}
}

class MessageHandlerMethod {

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	private final Class<?> messageClass;
	private final Class<?> declaringClass;
	private final String name;
	private final Class<?> returnedType;
	private final ParameterList parameters;
	private final MethodType type;
	private final boolean validable;

	public MessageHandlerMethod(final Class<?> messageClass, final Class<?> declartingClass, final String name, final Class<?> returnedType, final ParameterList parameters) {
		this.messageClass = messageClass;
		this.declaringClass = declartingClass;
		this.name = name;
		this.returnedType = returnedType;
		this.parameters = parameters;
		this.type = MethodType.methodType(returnedType, getParameterTypes(parameters));
		this.validable = hasValidableParameters(parameters);
	}

	private static final Class<?>[] getParameterTypes(final ParameterList parameters) {
		return parameters.stream()
			.map(ParameterEntry::getType)
			.collect(CollectorUtils.toListWithSameSizeAs(parameters))
			.toArray(EMPTY_CLASS_ARRAY);
	}

	private static final boolean hasValidableParameters(final ParameterList parameters) {
		return parameters.stream()
			.filter(ParameterEntry::isValidable)
			.findAny()
			.isPresent();
	}

	public Class<?> getMessageClass() {
		return messageClass;
	}

	public Class<?> getDeclaringClass() {
		return declaringClass;
	}

	public String getName() {
		return name;
	}

	public Class<?> getReturnedType() {
		return returnedType;
	}

	public boolean isValidable() {
		return validable;
	}

	public ParameterList getParameters() {
		return parameters;
	}

	public MethodType getType() {
		return type;
	}
}

class ParameterList extends ArrayList<ParameterEntry> {

	private static final long serialVersionUID = 1L;

	/**
	 * @param capacity initial capacity
	 */
	private ParameterList(final int capacity) {
		super(capacity);
	}

	/**
	 * @param types parameter types
	 * @param validate positions of parameters to validate
	 * @param assist positions of parameters to assist
	 */
	public static ParameterList of(final List<Class<?>> types, final Set<Short> validate, final Set<Short> assist) {

		final int parametersCount = types.size();
		final ParameterList parameters = new ParameterList(parametersCount);

		// we work with short because java method cannot have more parameters

		for (short i = 0; i < parametersCount; i++) {

			final Class<?> type = types.get(i);
			final boolean isValidable = validate.contains(i);
			final boolean isAssisted = assist.contains(i);

			parameters.add(new ParameterEntry(type, i, isValidable, isAssisted));
		}

		return parameters;
	}
}

class ParameterEntry {

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

	public short getPosition() {
		return position;
	}

	public boolean isValidable() {
		return validable;
	}

	public boolean isAssisted() {
		return assisted;
	}
}
