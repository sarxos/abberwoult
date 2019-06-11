package com.github.sarxos.abberwoult.exception;

import static java.util.stream.Collectors.joining;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;

import com.github.sarxos.abberwoult.annotation.Receives;


/**
 * This exception is thrown when message validation failed. A message validation happen only when
 * message argument is annotated with {@link Valid} annotation in a message handler which was
 * annotated with {@link Receives}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@SuppressWarnings("serial")
public class MessageHandlerValidationException extends RuntimeException {

	public <T> MessageHandlerValidationException(final Set<ConstraintViolation<T>> violations) {
		super("Message handler validation failed with the following errors:\n- " + violations.stream()
			.map(ConstraintViolation::getMessage)
			.collect(joining("\n- ")));
	}
}
