package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.ActorUtils.toActorClass;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getClazz;

import com.github.sarxos.abberwoult.annotation.Autostart;

import akka.actor.Actor;
import io.quarkus.runtime.annotations.Template;


/**
 * A {@link Template} used to record actor classes annotated with {@link Autostart} annotation.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Template
public class ActorStarterTemplate {

	/**
	 * Register actor class name to be started.
	 *
	 * @param className
	 */
	public void register(final String className) {

		final Class<?> clazz = getClazz(className);
		final Class<? extends Actor> actor = toActorClass(clazz);

		ActorStarter.register(actor);
	}
}
