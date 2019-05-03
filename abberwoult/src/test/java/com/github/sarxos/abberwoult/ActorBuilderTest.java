package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.MessageHandler;
import com.github.sarxos.abberwoult.trait.Comm;

import akka.actor.ActorRef;
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

	public static class OrdinaryActor extends SimpleActor implements Comm {

		@MessageHandler
		public void handleInteger(Integer i) {
			reply(i);
		}
	}

	public static class UnnamedActor extends SimpleActor implements Comm {

		@MessageHandler
		public void handleInteger(Integer i) {
			reply(self().path().name());
		}
	}

	@Named("bubu")
	public static class NamedActor extends SimpleActor implements Comm {

		@MessageHandler
		public void handleInteger(Integer i) {
			reply(self().path().name());
		}
	}

	@Inject
	ActorEngine engine;

	@Test
	void test_buildOridinaryActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(engine)
			.of(OrdinaryActor.class)
			.build();

		assertThat(askResult(ref, 1)).isEqualTo(1);

		kill(ref);
	}

	@Test
	void test_buildUnnamedActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(engine)
			.of(UnnamedActor.class)
			.build();

		assertThat(askResult(ref, 1).toString()).startsWith("$");

		kill(ref);
	}

	@Test
	void test_buildNamedActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(engine)
			.of(NamedActor.class)
			.build();

		assertThat(askResult(ref, 1)).isEqualTo("bubu");

		kill(ref);
	}

	@Test
	void test_buildNamedActorTwoce() throws Exception {

		final ActorRef ref = new ActorBuilder<>(engine)
			.of(NamedActor.class)
			.build();

		assertThatThrownBy(() -> new ActorBuilder<>(engine)
			.of(NamedActor.class)
			.build())
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
