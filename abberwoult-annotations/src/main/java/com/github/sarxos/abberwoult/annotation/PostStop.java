package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Annotation to mark methods which should be invoked after actor is stopped.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface PostStop {

}
