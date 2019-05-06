package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.ReflectionUtils.getClazz;

import com.github.sarxos.abberwoult.annotation.Receives;

import io.quarkus.runtime.annotations.Template;


/**
 * A {@link Template} used to record bytecode responsible for {@link Receives} registration.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Template
public class ActorRegistryTemplate {

	public void register(final String className) {

		final Class<?> clazz = getClazz(className);

		MessageHandlerRegistry.register(clazz);
		PreStartRegistry.register(clazz);
		PostStopRegistry.register(clazz);
	}
}
