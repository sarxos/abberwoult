package com.testing;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.ActorEngine;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.trait.Comm;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class MessageHandlerTest {

	public static class TestActor extends SimpleActor implements Comm {

		@MessageHandler
		public void handleInteger(Integer i) {
			reply(i);
		}
	}

	public static class TestActorSuperclass extends SimpleActor implements Comm {

		@MessageHandler
		public void handleInteger(Integer i) {
			reply(i);
		}
	}

	public static class TestActorSubclass extends TestActorSuperclass {

	}

	@Inject
	ActorEngine engine;

	// @Test
	void test_messageHandlerInClass() throws Exception {

		final ActorRef ref = engine.actor()
			.of(TestActor.class)
			.build();

		assertThat(askResult(ref, 1)).isEqualTo(1);

		kill(ref);

	}

	// @Test
	void test_messageHandlerInSuperclass() throws Exception {

		final ActorRef ref = engine.actor()
			.of(TestActorSubclass.class)
			.build();

		assertThat(askResult(ref, 1)).isEqualTo(1);

		kill(ref);

	}

	private Object askResult(final ActorRef ref, final Object message) throws Exception {
		final Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		final Future<Object> future = Patterns.ask(ref, message, timeout);
		return Await.result(future, timeout.duration());
	}

	private void kill(final ActorRef ref) {
		ref.tell(PoisonPill.getInstance(), ActorRef.noSender());
	}

}
