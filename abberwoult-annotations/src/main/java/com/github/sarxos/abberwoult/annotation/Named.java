package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;


/**
 * A name qualifier. This annotation uses relaxed binding policy. It means that the {@link #value()}
 * method is a {@link Nonbinding} so that there can be a single {@link Producer} for a beans with a
 * different {@link #value()}. Please do not confuse it with the {@link javax.inject.Named}, which
 * required {@link Producer} for each {@link #value()}, so e.g. when you have bean named A, and bean
 * named B, you will need two different producers. One for A and one for B. When using this
 * annotation this is not required and you may expose only one {@link Producer} for all bean
 * annotated with this specific annotation.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Qualifier
@Documented
@Target({ FIELD, PARAMETER, METHOD, TYPE })
@Retention(RUNTIME)
public @interface Named {

	public static final String UNKNOWN = "##unknown##";

	/**
	 * @return The class of actor to inject
	 */
	@Nonbinding
	String value() default UNKNOWN;
}
