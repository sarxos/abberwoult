package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.MessageHandler;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class CreateReceiveTest {

	static final class SomeMsg {

	}

	static final class TestActor extends SimpleActor {

		@MessageHandler
		void handleSomeMsg(final SomeMsg msg) {
			getSender().tell(1, getSelf());
		}
	}

	@Inject
	@ActorOf(TestActor.class)
	ActorRef ref;

	@Test
	void test_receiveCreated() throws Exception {

		final Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		final Future<Object> future = Patterns.ask(ref, new SomeMsg(), timeout);
		final Integer result = (Integer) Await.result(future, timeout.duration());

		assertThat(result).isEqualTo(1);
	}
}
