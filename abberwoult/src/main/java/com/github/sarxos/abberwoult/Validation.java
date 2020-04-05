package com.github.sarxos.abberwoult;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.dsl.Universe;
import com.github.sarxos.abberwoult.exception.MessageHandlerValidationException;


public class Validation {

	public static void validate(final Universe invoker, final Object message) {

		final ActorUniverse universe = invoker.getUniverse();
		final Validator validator = universe.validator();
		final Set<ConstraintViolation<Object>> violations = validator.validate(message);

		if (violations.isEmpty()) {
			return;
		}

		throw new MessageHandlerValidationException(violations);
	}

}
