package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

import akka.actor.Actor;


/**
 * This annotation is used to inject actor by its class.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Qualifier
@Documented
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface ActorByClass {

	/**
	 * @return The class of actor to inject
	 */
	@Nonbinding
	Class<? extends Actor> value() default Actor.class;
}
