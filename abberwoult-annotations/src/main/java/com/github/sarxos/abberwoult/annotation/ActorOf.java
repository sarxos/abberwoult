package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Inject;
import javax.inject.Qualifier;

import akka.actor.Actor;


/**
 * This annotation is used to inject actor reference by the actor class. This will cause new actor
 * to be created. Actors created with this method can only have no-arg default constructor or a
 * wired one (the one annotated with {@link Inject}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Qualifier
@Documented
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface ActorOf {

	/**
	 * Class to indicate that no valid class was set. When updating it just make sure to change
	 * default value below.
	 */
	public static abstract class NoActor implements Actor {
		// nothing interesting in here
	};

	/**
	 * @return The class of actor to inject
	 */
	@Nonbinding
	Class<? extends Actor> value() default NoActor.class;
}
