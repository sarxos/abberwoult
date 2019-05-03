package com.github.sarxos.abberwoult.deployment;

import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Valid;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.util.CollectorUtils;

import io.vavr.control.Option;


@Singleton
public class MessageHandlersRegistry {

	/**
	 * A static map where all {@link MessageHandler} annotation points are stored. This map is
	 * populated by a {@link MessageHandlersRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<Class<?>, Map<Class<?>, MessageHandlerMethod>> RECORDS = new HashMap<>();

	/**
	 * A validator instance used to validate message in case when {@link MessageHandler} annotated
	 * method contains an argument which was annotated with {@link Valid} annotation.
	 */
	@Inject
	Validator validator;

	static void store(final Class<?> declaringClass, final String handlerName, final Class<?> handlerType, final ParameterList parameters) {
		RECORDS
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

	/**
	 * Return a mapping between message class and corresponding handlers.
	 *
	 * @param declaringClass a declaring class
	 * @return A mapping between message class and corresponding handlers
	 */
	public Option<Map<Class<?>, MessageHandlerMethod>> getHandlersFor(final Class<?> declaringClass) {
		return Option.of(RECORDS.get(declaringClass));
	}

	/**
	 * This method will return a {@link MessageHandlerMethod} for a given message class in a given
	 * declaring class.
	 *
	 * @param declaringClass a declaring class
	 * @param messageClass a message class
	 * @return A {@link MessageHandlerMethod} entry
	 */
	public Option<MessageHandlerMethod> getHandlerFor(final Class<?> declaringClass, final Class<?> messageClass) {
		return getHandlersFor(declaringClass).map(mapping -> mapping.get(messageClass));
	}

	public static class MessageHandlerMethod {

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

	public static class ParameterList extends ArrayList<ParameterEntry> {

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

	public static class ParameterEntry {

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
}
