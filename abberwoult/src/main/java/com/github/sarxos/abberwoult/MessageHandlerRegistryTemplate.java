package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.MessageHandlerRegistry.store;
import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getClazz;

import java.util.List;
import java.util.Set;

import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import io.quarkus.runtime.annotations.Template;


/**
 * A {@link Template} used to record bytecode responsible for {@link MessageHandler} registration.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Template
public class MessageHandlerRegistryTemplate {

	/**
	 * Register {@link MessageHandler}.
	 *
	 * @param recipientClassName the message handler declaring class
	 * @param handlerName the message handler method name
	 * @param handlerTypeName the message handler return type name
	 * @param parameterTypeNames the parameter types
	 * @param validablePositions the indexes of parameters to validate
	 * @param assistedPositions the indexes of parameters to assist
	 */
	public void register(
		final String recipientClassName,
		final String handlerName,
		final String handlerTypeName,
		final List<String> parameterTypeNames,
		final Set<Short> parametersToValidate,
		final Set<Short> parametersToAssist) {

		final Class<?> recipientClass = getClazz(recipientClassName);
		final Class<?> handlerType = getClazz(handlerTypeName);

		final List<Class<?>> parameterTypes = parameterTypeNames.stream()
			.map(ReflectionUtils::getClazz)
			.collect(toListWithSameSizeAs(parameterTypeNames));

		final ParameterList parameters = ParameterList.of(parameterTypes, parametersToValidate, parametersToAssist);

		store(recipientClass, handlerName, handlerType, parameters);
	}
}
