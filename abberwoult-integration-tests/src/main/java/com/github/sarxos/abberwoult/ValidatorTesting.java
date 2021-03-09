package com.github.sarxos.abberwoult;

import javax.inject.Inject;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ValidatorTesting {

	public static final class ValidatorGetMsg {
	}

	public static final class TestActor extends SimpleActor implements Utils {

		@Inject
		Validator validator;

		public void handleValidatorGetMsg(@Received final ValidatorGetMsg msg) {
			reply(validator);
		}
	}
}
