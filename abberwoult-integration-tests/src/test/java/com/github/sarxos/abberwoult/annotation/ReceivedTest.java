package com.github.sarxos.abberwoult.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class ReceivedTest {

	public static class TestActor extends SimpleActor implements Utils {

		public void handleInteger(@Receives Integer i) {
			reply(i);
		}
	}

	public static class TestActorSuperclass extends SimpleActor implements Utils {

		public void handleIntegerXXX(@Receives Integer i) {
			reply(i);
		}
	}

	public static class TestActorSubclass extends TestActorSuperclass {

	}

	@Inject
	ActorUniverse universe;

	// @Test
	void test_messageHandlerInClass() throws Exception {

		final ActorRef ref = universe.actor()
			.of(TestActor.class)
			.create();

		assertThat(askResult(ref, 1)).isEqualTo(1);

		kill(ref);

	}

	// @Test
	void test_messageHandlerInSuperclass() throws Exception {

		final ActorRef ref = universe.actor()
			.of(TestActorSubclass.class)
			.create();

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
