package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.util.ActorUtils.getMailboxId;
import static com.github.sarxos.abberwoult.util.ActorUtils.getMessageDispatcherId;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.cdi.BeanLocator;

import akka.actor.Actor;
import akka.actor.Props;


/**
 * This is injectable factory service which creates {@link Props} instances used to build actors.
 * Every {@link Props} object created by this factory keeps reference to the special
 * {@link ActorCreator} responsible for creating and wiring actor instances. This factory can be
 * injected but does not have to.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class Propser {

	/**
	 * A {@link BeanLocator} used by {@link ActorCreator} to process injections in actor class.
	 */
	private final BeanLocator locator;

	/**
	 * Create new {@link Propser} factory.
	 *
	 * @param locator the {@link BeanLocator} used to wire actor instances
	 */
	@Inject
	public Propser(final BeanLocator locator) {
		this.locator = locator;
	}

	/**
	 * Create {@link Props} for given actor class.
	 *
	 * @param clazz the actor's class
	 * @param args the optional arguments to be passed down to actor constructor
	 * @return Actor {@link Props}
	 */
	public <T extends Actor> Props props(final Class<T> clazz, final Object... args) {
		return Props
			.create(clazz, new ActorCreator<T>(locator, clazz, args))
			.withDispatcher(getMessageDispatcherId(clazz))
			.withMailbox(getMailboxId(clazz));
	}
}
