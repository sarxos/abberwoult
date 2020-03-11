package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import akka.event.EventStream;


/**
 * Automatically subscribes actor to the annotated event dispatched from {@link EventStream}. All
 * annotated events type will be automatically received by this actor.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface Event {

}
