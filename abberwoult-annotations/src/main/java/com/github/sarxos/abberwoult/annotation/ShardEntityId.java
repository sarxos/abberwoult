package com.github.sarxos.abberwoult.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;


/**
 * This annotation should be placed on a shard routable message field which is to be used as a shard
 * ID when routing message to a shard entity.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Documented
@Target({ FIELD, METHOD })
@Retention(RUNTIME)
public @interface ShardEntityId {

}
