package com.github.sarxos.abberwoult.util;

import javax.inject.Named;

import akka.actor.Actor;
import io.vavr.control.Option;


public class ActorUtils {

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

}
