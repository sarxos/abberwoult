package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.validation.Validator;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.trait.Comm;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class ValidatorTest {

	static final class ValidatorGetMsg {
	}

	static final class TestActor extends SimpleActor implements Comm {

		@Inject
		Validator validator;

		public void handleValidatorGetMsg(@Received final ValidatorGetMsg msg) {
			reply(validator);
		}
	}

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
