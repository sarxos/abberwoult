package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import akka.actor.Actor;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * This annotation is used to inject actor by its class.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target({ FIELD, PARAMETER })
@Retention(RUNTIME)
public @interface ByClass {

	/**
	 * @return The class of actor to inject
	 */
	Class<? extends Actor> value();
}
