package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.util.ReflectionUtils.getAnnotationFromClass;

import akka.actor.Actor;
import akka.actor.Props;
import akka.dispatch.Dispatchers;
import akka.dispatch.Mailboxes;
import com.github.sarxos.abberwoult.annotation.Dispatcher;
import com.github.sarxos.abberwoult.annotation.Mailbox;
import io.vavr.control.Option;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;


/**
 * This is factory which creates {@link Props} instances that are used to build actors. Every
 * {@link Props} object created by this factory keeps reference to the special {@link ActorCreator}
 * designed to instantiate actor and wire it (inject all required injectees). This factory can be
 * used as injectable {@link Service}, but does not have to.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class Propser {

	/**
	 * A {@link ServiceLocator} used by {@link ActorCreator} to wire actors.
	 */
	private final BeanManager bm;

	/**
	 * Create new {@link Propser} factory.
	 *
	 * @param locator the {@link ServiceLocator} used to wire actor instances
	 */
	@Inject
	public Propser(final BeanManager bm) {
		this.bm = bm;
	}

	/**
	 * Create {@link Props} for given actor class.
	 *
	 * @param clazz the actor's class
	 * @param args the optional arguments to be passed down to actor constructor
	 * @return Actor {@link Props}
	 */
	public Props props(final Class<? extends Actor> clazz, final Object... args) {
		return Props
			.create(new ActorCreator<>(bm, clazz, args))
			.withDispatcher(getDispatcherFromClass(clazz))
			.withMailbox(getMailboxFromClass(clazz));
	}

	/**
	 * Return dispatcher name to be used by given actor or default dispatcher ID if no
	 * {@link Dispatcher} annotation is present on class.
	 *
	 * @param clazz the class to get dispatcher name from
	 * @return Dispatcher name
	 */
	private static final String getDispatcherFromClass(final Class<? extends Actor> clazz) {
		return Option
			.of(getAnnotationFromClass(clazz, Dispatcher.class))
			.map(Dispatcher::value)
			.getOrElse(() -> Dispatchers.DefaultDispatcherId());
	}

	/**
	 * Return mailbox name to be used by given actor or default mailbox ID if no {@link Mailbox}
	 * annotation is present on class.
	 *
	 * @param clazz the class to get mailbox name from
	 * @return Dispatcher name
	 */
	private static final String getMailboxFromClass(final Class<? extends Actor> clazz) {
		return Option
			.of(getAnnotationFromClass(clazz, Mailbox.class))
			.map(Mailbox::value)
			.getOrElse(() -> Mailboxes.DefaultMailboxId());
	}
}
