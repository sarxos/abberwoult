package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * This annotation should be used to annotate sharded actors belonging to a sharding.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target({ TYPE })
@Retention(RUNTIME)
public @interface Sharded {

	/**
	 * @return The sharding name
	 */
	String name();

	/**
	 * @return True if sharding should be automatically started
	 */
	boolean autostart() default true;
}
