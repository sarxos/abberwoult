package com.github.sarxos.abberwoult.hk2;

import akka.actor.ActorSystem;
import com.github.sarxos.abberwoult.SystemSettings;
import com.typesafe.config.Config;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.glassfish.hk2.api.Factory;
import org.jvnet.hk2.annotations.Service;


/**
 * This is {@link Factory} for {@link ActorSystem}. This factory creates only one instance of
 * {@link ActorSystem} which is bound to {@link Singleton} scope. Whenever {@link ActorSystem} is
 * injected into the context it will be the very same instance.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Service
public class ActorSystemFactory implements Factory<ActorSystem> {

	private final SystemSettings settings;
	private final Provider<Config> config;

	@Inject
	public ActorSystemFactory(final SystemSettings settings, final Provider<Config> config) {
		this.settings = settings;
		this.config = config;
	}

	/**
	 * Create new actor system. Please note the {@link Singleton} annotation in here which will make
	 * provided {@link ActorSystem} a {@link Singleton}-scoped.
	 *
	 * @return New {@link ActorSystem}
	 * @see Factory#provide()
	 */
	@Override
	@Singleton
	public ActorSystem provide() {
		return ActorSystem.create(settings.getName(), config.get());
	}

	@Override
	public void dispose(final ActorSystem instance) {
		// do nothing, ActorSystem should be terminated by coordinated shutdown
	}
}
