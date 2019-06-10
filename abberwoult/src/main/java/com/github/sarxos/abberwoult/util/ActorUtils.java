package com.github.sarxos.abberwoult.util;

import static com.github.sarxos.abberwoult.util.ReflectionUtils.getAnnotationFromClass;

import java.util.Objects;

import javax.inject.Named;

import com.github.sarxos.abberwoult.annotation.Dispatcher;
import com.github.sarxos.abberwoult.annotation.Mailbox;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.dispatch.Dispatchers;
import akka.dispatch.Mailboxes;
import io.vavr.control.Option;


public class ActorUtils {

	private static final String USER = "user";

	public static final String DEFAULT_MAILBOX_ID = Mailboxes.DefaultMailboxId();
	public static final String DEFAULT_MESSAGE_DISPATCHER_ID = Dispatchers.DefaultDispatcherId();
	public static final String DEFAULT_TIMEOUT_SECONDS = "10";
	public static final String DEFAULT_TIMEOUT_PROP = "akka.default-timeout";

	/**
	 * Return actor name. Only the latest actor in inheritance tree is scanned for name.
	 *
	 * @param clazz the class to get actor name from
	 * @return Actor name
	 */
	public static Option<String> getActorName(final Class<? extends Actor> clazz) {
		return Option
			.of(clazz.getAnnotation(Named.class))
			.map(Named::value);
	}

	/**
	 * Return dispatcher name to be used by given actor or default dispatcher ID if no
	 * {@link Dispatcher} annotation is present on class.
	 *
	 * @param clazz the class to get dispatcher name from
	 * @return Dispatcher name
	 */
	public static final String getMessageDispatcherId(final Class<? extends Actor> clazz) {
		return Option
			.of(getAnnotationFromClass(clazz, Dispatcher.class))
			.map(Dispatcher::value)
			.getOrElse(DEFAULT_MESSAGE_DISPATCHER_ID);
	}

	/**
	 * Return mailbox name to be used by given actor or default mailbox ID if no {@link Mailbox}
	 * annotation is present on class.
	 *
	 * @param clazz the class to get mailbox name from
	 * @return Dispatcher name
	 */
	public static final String getMailboxId(final Class<? extends Actor> clazz) {
		return Option
			.of(getAnnotationFromClass(clazz, Mailbox.class))
			.map(Mailbox::value)
			.getOrElse(DEFAULT_MAILBOX_ID);
	}

	public static String getActorPath(final Class<? extends Actor> clazz) {
		final String name = getActorName(clazz).getOrElse(clazz::getName);
		final String path = "/" + USER + "/" + name;
		return path;
	}

	public static boolean equals(final ActorRef a, final ActorRef b) {
		return Objects.equals(a.path(), b.path());
	}
}
