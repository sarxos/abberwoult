package com.github.sarxos.abberwoult.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class ReceivesIT {

	@Inject
	ActorUniverse universe;

	@Test
	void test_messageHandlerInClass() throws Exception {

		final ActorRef ref = universe.actor()
			.of(ReceivesActor.class)
			.create();

		assertThat(askResult(ref, 1)).isEqualTo(11);

		kill(ref);

	}

	@Test
	void test_messageHandlerInSuperclass() throws Exception {

		final ActorRef ref = universe.actor()
			.of(ReceivesActorSubclass.class)
			.create();

		assertThat(askResult(ref, 1)).isEqualTo(22);

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
