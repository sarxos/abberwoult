package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;

import akka.actor.Actor;


@Documented
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface GuardedBy {

	/**
	 * @return The class of actor to inject
	 */
	@Nonbinding
	Class<? extends Actor> value();
}
