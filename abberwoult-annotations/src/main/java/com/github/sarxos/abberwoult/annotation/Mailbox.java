package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * This annotation is used to instruct actor factories to use specific mailbox when creating
 * actor props. This annotation can be put on actor's class, on its superclass or any interface this
 * actor implements. In case when this annotation is put on multiple locations the first found
 * annotation is used (starting from the bottom, i.e. the actor's class). In case when this
 * annotation is not placed on actor's class, a default mailbox will be used.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Mailbox {

	/**
	 * @return The mailbox name
	 */
	String value();
}
