package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Actors annotated with this annotation will be automatically started when application is launched.
 * Please note that actor annotated with this annotation need to have no-arg constructor present. If
 * such constructor is not present the exception is thrown in augmentation phase. Such actors also
 * need to be identifiable in runtime and therefore require {@link Labeled} annotation to be put on
 * the class.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Autostart {

}
