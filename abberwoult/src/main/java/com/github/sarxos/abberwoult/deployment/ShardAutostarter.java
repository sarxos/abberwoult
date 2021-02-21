package com.github.sarxos.abberwoult.deployment;

import static java.util.Collections.synchronizedSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.annotation.Sharded;
import com.github.sarxos.abberwoult.util.ActorUtils;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.cluster.sharding.Shard;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.Recorder;


/**
 * This is a singleton bean which intercepts {@link StartupEvent} and start all actors annotated
 * with the {@link Sharded} annotation which has {@link Sharded#autostart()} set to true (it's set
 * to true by default).
 *
 * @author Bartosz Firyn (sarxos)
 */
@Recorder
@Singleton
public class ShardAutostarter {

	private static final Logger LOG = Logger.getLogger(ShardAutostarter.class);

	/**
	 * The list of {@link Actor} classes annotated with {@link Shard} annotation, detected in
	 * augmentation phase.
	 */
	private static final Collection<String> SHARDS = synchronizedSet(new LinkedHashSet<>());

	/**
	 * A mapping between class name and corresponding actor reference.
	 */
	private final Map<String, ActorRef> references = new HashMap<>();

	/**
	 * Injected actor universe which is used to spawn actor instances.
	 */
	private final ActorUniverse universe;

	/**
	 * Constructor used when this class is instantiated as a {@link Singleton}.
	 *
	 * @param universe the actor universe later used to spawn actor instances
	 */
	@Inject
	public ShardAutostarter(final ActorUniverse universe) {
		this.universe = universe;
	}

	/**
	 * Constructor used when this class is instantiated as a {@link Recorder}.
	 */
	public ShardAutostarter() {
		this.universe = null;
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
	 * Register autostartable actor class.
	 *
	 * @param clazz the actor class
	 */
	public void register(final String clazz) {
		SHARDS.add(clazz);
		LOG.infof("Registered actor %s", clazz);
	}

	private void startAllRecordedActors() {
		LOG.infof("Autostarting actors %s", SHARDS.size());
		SHARDS.stream()
			.map(this::loadClass)
			.peek(clazz -> LOG.infof("Autostarting sharding %s", clazz))
			.forEach(clazz -> references.computeIfAbsent(clazz.getName(), start(clazz)));
	}

	private Class<? extends Actor> loadClass(String clazz) {
		return ActorUtils.toActorClass(ReflectionUtils.getClazz(clazz));
	}

	private Function<String, ActorRef> start(final Class<? extends Actor> clazz) {
		return className -> universe.actor()
			.of(clazz)
			.create();
	}

	public ActorRef getRef(final Class<? extends Actor> clazz) {
		return references.get(clazz.getName());
	}
}
