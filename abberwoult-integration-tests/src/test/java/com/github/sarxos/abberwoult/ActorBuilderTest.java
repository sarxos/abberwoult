package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.trait.Utils;

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

	public static class OrdinaryActor extends SimpleActor implements Utils {

		public void handleInteger(@Receives Integer i) {
			reply(i);
		}
	}

	public static class UnnamedActor extends SimpleActor implements Utils {

		public void handleInteger(@Receives Integer i) {
			reply(self().path().name());
		}
	}

	@Labeled("bubu")
	public static class NamedActor extends SimpleActor implements Utils {

		public void handleInteger(@Receives Integer i) {
			reply(self().path().name());
		}
	}

	@Inject
	Propser propser;

	@Inject
	ActorSystem system;

	@Test
	void test_buildOridinaryActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(propser, system)
			.of(OrdinaryActor.class)
			.build();

		assertThat(askResult(ref, 1)).isEqualTo(1);

		kill(ref);
	}

	@Test
	void test_buildUnnamedActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(propser, system)
			.of(UnnamedActor.class)
			.build();

		assertThat(askResult(ref, 1).toString()).startsWith("$");

		kill(ref);
	}

	@Test
	void test_buildNamedActor() throws Exception {

		final ActorRef ref = new ActorBuilder<>(propser, system)
			.of(NamedActor.class)
			.build();

		assertThat(askResult(ref, 1)).isEqualTo("bubu");

		kill(ref);
	}

	@Test
	void test_buildNamedActorTwice() throws Exception {

		final ActorRef ref = new ActorBuilder<>(propser, system)
			.of(NamedActor.class)
			.build();

		assertThatThrownBy(() -> new ActorBuilder<>(propser, system)
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
