package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.ReflectionUtils.getClazz;

import com.github.sarxos.abberwoult.annotation.MessageHandler;

import io.quarkus.runtime.annotations.Template;


/**
 * A {@link Template} used to record bytecode responsible for {@link MessageHandler} registration.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Template
public class MessageHandlerRegistryTemplate {

	public void register(final String className) {
		MessageHandlerRegistry.register(getClazz(className));
	}
}
