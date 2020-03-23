package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * Annotation used on synthetic runtime constructs to let developers know that given element was
 * generated in class instrumentation phase.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target({ TYPE, METHOD, FIELD })
@Retention(RUNTIME)
public @interface Generated {
}
