package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Inject;


/**
 * This annotation is used to mark assisted arguments when both {@link Inject} and supplied list of
 * arguments is used to create actor. Arguments which are present in the supplied list should be
 * annotated with this annotation and arguments which are injected should be leaved unannotated. A
 * typical use case is the following:
 *
 * <pre>
 * &#64;Inject
 * public MyActor(@Assisted int count, BeanLocator locator) {
 * 	this.count = count;
 * 	this.locator = locator;
 * }
 * </pre>
 *
 * As you can see only <code>count</code> argument is supplied to the builder. The
 * <code>BeanLocator</code> is taken automatically by CDI.
 *
 * <pre>
 * final int count = 123;
 * universe.actor()
 * 	.of(MyActor.class)
 * 	.withArguments(count)
 * 	.build();
 * </pre>
 *
 * @author Bartosz Firyn (sarxos)
 */
@Target({ PARAMETER })
@Retention(RUNTIME)
public @interface Assisted {

}
