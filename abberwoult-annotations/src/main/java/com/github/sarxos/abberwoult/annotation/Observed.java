package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Automatically subscribes actor to the annotated event. By doing this all events of a given type
 * will be automatically received by this actor. Using this annotation cause additionally the same
 * effects as using {@link Receives}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Observed {

}
