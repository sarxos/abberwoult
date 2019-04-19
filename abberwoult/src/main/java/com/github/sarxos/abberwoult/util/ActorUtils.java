package com.github.sarxos.abberwoult.util;

import javax.inject.Named;

import akka.actor.Actor;
import io.vavr.control.Option;


public class ActorUtils {

	private static final String USER = "user";

	/**
	 * Return actor name.
	 *
	 * @param clazz the class to get actor name from
	 * @return Actor name
	 */
	public static Option<String> getActorName(final Class<? extends Actor> clazz) {
		return Option
			.of(clazz.getAnnotation(Named.class))
			.map(Named::value);
	}

	public static String getActorPath(final Class<? extends Actor> clazz) {
		final String name = getActorName(clazz).getOrElse(clazz::getName);
		final String path = "/" + USER + "/" + name;
		return path;
	}

}
