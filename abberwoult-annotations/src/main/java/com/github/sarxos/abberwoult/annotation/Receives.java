package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * This annotation is to annotate method parameter which will be used to receive message by the
 * actor.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Receives {

}
