package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.MessageHandlerRegistry.store;
import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static java.util.Collections.emptySet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.quarkus.runtime.annotations.Template;


@Template
public class MessageHandlerRegistryTemplate {

	@SuppressWarnings("serial")
	public final static Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>() {
		{
			put("void", void.class);
			put("boolean", boolean.class);
			put("byte", byte.class);
			put("short", short.class);
			put("char", char.class);
			put("int", int.class);
			put("long", long.class);
			put("float", float.class);
			put("double", double.class);
		}
	};

	public void register(
		final String recipientClassName,
		final String handlerName,
		final String handlerTypeName,
		final List<String> parameterTypeNames,
		final Set<Short> validablePositions,
		final Set<Short> assistedPositions) {

		final Class<?> recipientClass = asClazz(recipientClassName);
		final Class<?> handlerType = asClazz(handlerTypeName);

		final List<Class<?>> parameterTypes = parameterTypeNames.stream()
			.map(this::asClazz)
			.collect(toListWithSameSizeAs(parameterTypeNames));

		final Set<Short> validablePositionsSet = validablePositions.isEmpty() ? emptySet() : validablePositions;
		final Set<Short> assistedPositionsSet = assistedPositions.isEmpty() ? emptySet() : assistedPositions;

		store(recipientClass, handlerName, handlerType, parameterTypes, validablePositionsSet, assistedPositionsSet);
	}

	private Class<?> asClazz(final String clazzName) {
		if (PRIMITIVES.containsKey(clazzName)) {
			return PRIMITIVES.get(clazzName);
		}
		try {
			return Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
}
