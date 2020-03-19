package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorBuilderTesting.ReplyIntegerActor;
import com.github.sarxos.abberwoult.ActorBuilderTesting.ReplyOathNamedActor;
import com.github.sarxos.abberwoult.ActorBuilderTesting.ReplyPathAnnonymousActor;
import com.github.sarxos.abberwoult.builder.ActorBuilder;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.InvalidActorNameException;
import akka.actor.PoisonPill;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class ActorBuilderTest {

	@Inject
	Propser propser;

	@Inject
	ActorSystem system;

	@Inject
	ActorUniverse universe;

	@Test
	void test_buildOridinaryActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(universe)
			.of(ReplyIntegerActor.class)
			.create();

		assertThat(askResult(ref, 1)).isEqualTo(1);

		kill(ref);
	}

	@Test
	void test_buildUnnamedActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(universe)
			.of(ReplyPathAnnonymousActor.class)
			.create();

		assertThat(askResult(ref, 1).toString()).startsWith("$");

		kill(ref);
	}

	@Test
	void test_buildNamedActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(universe)
			.of(ReplyOathNamedActor.class)
			.create();

		assertThat(askResult(ref, 1)).isEqualTo("bubu");

		kill(ref);
	}

	@Test
	void test_buildNamedActorTwice() throws Exception {

		final ActorRef ref = new ActorBuilder<>(universe)
			.of(ReplyOathNamedActor.class)
			.create();

		assertThatThrownBy(() -> new ActorBuilder<>(universe)
			.of(ReplyOathNamedActor.class)
			.create())
				.isInstanceOf(InvalidActorNameException.class)
				.hasMessage("actor name [bubu] is not unique!");

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
