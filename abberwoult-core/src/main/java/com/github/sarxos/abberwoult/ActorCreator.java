package com.github.sarxos.abberwoult;


import akka.actor.Actor;
import akka.japi.Creator;

import com.github.sarxos.abberwoult.cdi.Injector;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * This is actor creator which crafts wired actor instances.
 *
 * @author Bartosz Firyn (sarxos)
 * @param <T>
 */
public class ActorCreator<T extends Actor> extends Injector<T> implements Creator<T> {

	/**
	 * Serial.
	 */
	private static final long serialVersionUID = 1L;

	private static final Class<?> STOP_CLASS = SimpleActor.class.getSuperclass();
	
	/**
	 * Creates new {@link ActorCreator} instance with given {@link ServiceLocator} and class which
	 * describes the actor to be created. Constructor takes also vararg objects list which are the
	 * arguments (optional) to be passed down to the actor constructor. Please note that in case
	 * when actor constructor is annotated with {@link Inject}, the arguments list will be ignored
	 * (creator will try to resolve all necessary arguments using dependency injection support and
	 * original arguments are ignored).
	 *
	 * @param bm the service locator to be used to wire created instance
	 * @param clazz the actor class which should be created
	 * @param args the actor constructor arguments (optional, ignored if {@link Inject} used)
	 */
	public ActorCreator(final BeanManager bm, final Class<T> clazz, final Object... args) {
		super(bm, clazz, STOP_CLASS, args);
	}
}
