package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.ActorUtils.toActorClass;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getClazz;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.ActorSystemUniverse;
import com.github.sarxos.abberwoult.annotation.Autostart;

import akka.actor.Actor;
import akka.actor.ActorRef;
import io.quarkus.runtime.StartupEvent;


/**
 * This is a singleton bean which intercepts {@link StartupEvent} and start all actors annotated
 * with the {@link Autostart} annotation. Please note that such actor need to have no-arg
 * constructor to be started and if such constructor is missing an exception is being thrown.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ActorStarter {

	private static final Logger LOG = Logger.getLogger("abberwoult-autostarter");

	/**
	 * The list of {@link Actor} classes annotated with {@link Autostart} annotation detected in
	 * augmentation phase.
	 */
	private static final Collection<Class<? extends Actor>> AUTOSTARTABLES = new LinkedHashSet<>();

	/**
	 * A mapping between class name and corresponding actor reference.
	 */
	private final Map<String, ActorRef> references = new HashMap<>();

	/**
	 * Injected actor universe which is used to spawn actor instances.
	 */
	private final ActorSystemUniverse universe;

	/**
	 * @param universe the actor universe later used to spawn actor instances
	 */
	@Inject
	ActorStarter(final ActorSystemUniverse universe) {
		this.universe = universe;
	}

	/**
	 * Event interceptor which listen for {@link StartupEvent} to be emitted and start all recorded
	 * autostartable actors.
	 *
	 * @param event the event to be intercepted
	 */
	void onStart(@Observes final StartupEvent event) {
		startAllRecordedActors();
	}

	/**
	 * Register actor class name to be started.
	 *
	 * @param className
	 */
	public void register(final String className) {

		final Class<?> clazz = getClazz(className);
		final Class<? extends Actor> actor = toActorClass(clazz);

		register(actor);
	}

	/**
	 * Register autostartable actor class.
	 *
	 * @param clazz the actor class
	 */
	static void register(final Class<? extends Actor> clazz) {
		AUTOSTARTABLES.add(clazz);
	}

	private void startAllRecordedActors() {
		AUTOSTARTABLES.stream()
			.peek(clazz -> LOG.infof("Autostarting actor %s", clazz))
			.forEach(clazz -> references.computeIfAbsent(clazz.getName(), start(clazz)));
	}

	private Function<String, ActorRef> start(final Class<? extends Actor> clazz) {
		return name -> universe.actor()
			.of(clazz)
			.build();
	}
}
