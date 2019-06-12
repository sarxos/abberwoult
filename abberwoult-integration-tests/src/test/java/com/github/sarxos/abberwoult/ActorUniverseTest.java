package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.testkit.TestKit;
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
public class ActorUniverseTest {

	@Inject
	@Labeled("test")
	Topic topic;

	@Inject
	ActorSystemUniverse universe;

	@Inject
	TestKit testkit;

	public static class TestActor extends SimpleActor implements Comm {
		public void onInteger(@Receives Integer i) {
			reply(i);
		}
	}

	@Labeled("foo")
	public static class TestNamedFooActor extends SimpleActor implements Comm {

		public void onInteger(@Receives Integer i) {
			reply(i);
		}

		public void onString(@Receives String s) {
			reply(self().path().toString());
		}
	}

	@Labeled
	public static class TestNamedActor extends SimpleActor implements Comm {
		public void onInteger(@Receives Integer i) {
			reply(i);
		}
	}

	@Test
	public void test_createActor() throws Exception {

		final ActorRef ref = universe.actor()
			.of(TestActor.class)
			.build();

		assertThat(askResult(ref, 2)).isEqualTo(2);

		kill(ref);
	}

	@Test
	public void test_createNamedActorOnceIsPossible() throws Exception {

		final ActorRef foo = universe.actor()
			.of(TestNamedFooActor.class)
			.build();

		assertThat(askResult(foo, 2)).isEqualTo(2);

		kill(foo);
	}

	@Test
	public void test_namedActorPathIsCorrect() throws Exception {

		final ActorRef foo = universe.actor()
			.of(TestNamedFooActor.class)
			.build();

		assertThat(foo.path().toString()).endsWith("/user/foo");

		kill(foo);
	}

	@Test
	public void test_namedActorWithNoValuePathIsCorrect() throws Exception {

		final ActorRef foo = universe.actor()
			.of(TestNamedActor.class)
			.build();

		assertThat(foo.path().toString()).endsWith("/user/" + TestNamedActor.class.getName());

		kill(foo);
	}

	@Test
	public void test_createNamedActorTwiceRaiseException() throws Exception {

		final ActorRef foo = universe.actor()
			.of(TestNamedFooActor.class)
			.build();

		assertThat(askResult(foo, 4)).isEqualTo(4);

		Assertions
			.assertThatThrownBy(() -> universe.actor()
				.of(TestNamedFooActor.class)
				.build())
			.isInstanceOf(InvalidActorNameException.class);

		kill(foo);
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
