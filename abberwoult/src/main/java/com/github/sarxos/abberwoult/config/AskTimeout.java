package com.github.sarxos.abberwoult.config;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.time.Duration;

import javax.inject.Qualifier;

import akka.util.Timeout;
import scala.concurrent.duration.FiniteDuration;


/**
 * Qualifier used to inject ask timeout. This qualifier can be used to inject Java {@link Duration},
 * Akka {@link Timeout} or Scala {@link FiniteDuration}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Qualifier
@Documented
@Target({ FIELD, PARAMETER, METHOD })
@Retention(RUNTIME)
public @interface AskTimeout {
}
