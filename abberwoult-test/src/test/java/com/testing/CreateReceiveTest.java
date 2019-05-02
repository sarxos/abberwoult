package com.testing;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
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

	public static final class SomeMsg {
	}

	public static final class SomeIntMsg {

		@Min(1)
		private final int i;

		public SomeIntMsg(int i) {
			this.i = i;
		}
	}

	public static final class TestActor extends SimpleActor {

		@MessageHandler
		public void handleSomeMsg(final SomeMsg msg) {
			sender().tell(1, self());
		}

		@MessageHandler
		public void handleSomeIntMsg(final @Valid SomeIntMsg msg) {
			sender().tell(msg.i, self());
		}

		@MessageHandler
		public void handleInteger(final Integer number) {
			sender().tell(number, self());
		}
	}

	@Inject
	@ActorOf(TestActor.class)
	ActorRef ref;

	private Object askResult(final Object message) throws Exception {
		final Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		final Future<Object> future = Patterns.ask(ref, message, timeout);
		return Await.result(future, timeout.duration());
	}

	@Test
	public void test_receiveBuilderForClass() throws Exception {
		assertThat(askResult(new SomeMsg())).isEqualTo(1);
	}

	@Test
	public void test_receiveBuilderForInteger() throws Exception {
		assertThat(askResult(1)).isEqualTo(1);
	}

	@Test
	public void test_receiveValidMsg() throws Exception {
		assertThat(askResult(new SomeIntMsg(1))).isEqualTo(1);
	}

	// @Test
	// public void test_receiveInvalidMsg() throws Exception {
	// assertThat(askResult(new SomeIntMsg(0))).isInstanceOf(Throwable.class);
	// }
}
