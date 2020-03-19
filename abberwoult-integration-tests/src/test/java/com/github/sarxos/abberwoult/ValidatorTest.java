package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ValidatorTesting.TestActor;
import com.github.sarxos.abberwoult.ValidatorTesting.ValidatorGetMsg;
import com.github.sarxos.abberwoult.annotation.ActorOf;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class ValidatorTest {

	@Inject
	Validator validator;

	@Inject
	@ActorOf(TestActor.class)
	ActorRef ref;

	@Test
	void test_injectValidatorInBean() {
		assertThat(validator).isNotNull();
	}

	@Test
	void test_injectValidatorInActor() throws Exception {

		final Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		final Future<Object> future = Patterns.ask(ref, new ValidatorGetMsg(), timeout);
		final Validator result = (Validator) Await.result(future, timeout.duration());

		assertThat(result).isNotNull();
	}
}
