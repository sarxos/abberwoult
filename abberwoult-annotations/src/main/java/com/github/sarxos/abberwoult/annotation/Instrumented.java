package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Instrumented {
	// nothing here
}
