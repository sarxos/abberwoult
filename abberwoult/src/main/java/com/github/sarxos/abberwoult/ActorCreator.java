package com.github.sarxos.abberwoult;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.cdi.BeanFactory;
import com.github.sarxos.abberwoult.cdi.BeanLocator;

import akka.actor.Actor;
import akka.japi.Creator;


/**
 * This is actor creator which crafts wired actor instances.
 *
 * @author Bartosz Firyn (sarxos)
 * @param <T>
 */
@SuppressWarnings("serial")
public class ActorCreator<T extends Actor> extends BeanFactory<T> implements Creator<T> {

	private static final Class<?> STOP_CLASS = SimpleActor.class.getSuperclass();

	/**
	 * Creates new {@link ActorCreator} instance with given {@link BeanLocator} and class which
	 * describes the actor to be created. Constructor takes also vararg objects list which are the
	 * arguments (optional) to be passed down to the actor constructor. Please note that in case
	 * when actor constructor is annotated with {@link Inject}, the arguments list will be ignored
	 * (creator will try to resolve all necessary arguments using dependency injection support and
	 * original arguments are ignored).
	 *
	 * @param locator the {@link BeanLocator} used to locate injectable beans
	 * @param clazz the actor class which should be created
	 * @param args the actor constructor arguments (optional, ignored if {@link Inject} used)
	 */
	public ActorCreator(final BeanLocator locator, final Class<T> clazz, final Object... args) {
		super(locator, clazz, STOP_CLASS, args);
	}
}
