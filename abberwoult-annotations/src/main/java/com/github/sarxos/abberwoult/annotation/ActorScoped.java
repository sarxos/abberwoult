package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Scope;


@Scope
@Inherited
@Documented
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface ActorScoped {

	/**
	 * Supports inline instantiation of the {@link ActorScoped} annotation.
	 */
	@SuppressWarnings("all")
	public final static class Literal extends AnnotationLiteral<ActorScoped> implements ActorScoped {
		private static final long serialVersionUID = 1L;
		public static final Literal INSTANCE = new Literal();
	}
}
