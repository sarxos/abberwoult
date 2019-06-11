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


/**
 * A name qualifier for distributed pubsub channels.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Qualifier
@Documented
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface Labeled {

	public static final String UNKNOWN = "##unknown##";

	/**
	 * @return The class of actor to inject
	 */
	@Nonbinding
	String value() default UNKNOWN;
}