package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Named;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorSystemUniverse;
import com.github.sarxos.abberwoult.AskableActorSelection;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.trait.Comm;
import com.github.sarxos.abberwoult.trait.Disposing;

import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.concurrent.Future;


@QuarkusTest
public class AskableActorSelectionTest {

	@Inject
	ActorSystemUniverse universe;

	@Inject
	@ActorOf(TestClass.class)
	AskableActorSelection selection;

	@Named("test")
	public static class TestClass extends SimpleActor implements Comm, Disposing {

		public void handleInteger(@Received final Integer i) {
			replyAndDispose(i);
		}

		public void handleThrowable(@Received final Throwable t) {
			replyAndDispose(t);
		}

		private void replyAndDispose(final Object value) {
			reply(value);
			dispose();
		}
	}

	@BeforeEach
	public void setup() {
		universe.actor()
			.of(TestClass.class)
			.build();
	}

	@AfterEach
	public void teardown() {
		selection.tell(PoisonPill.getInstance());
	}

	@Test
	public void test_ask() throws Exception {

		final CompletionStage<Integer> result = selection.ask(22);

		Assertions
			.assertThat(result.toCompletableFuture().get(3, TimeUnit.SECONDS))
			.isEqualTo(22);
	}

	@Test
	public void test_askWithException() throws Exception {

		final CompletionStage<String> result = selection.ask(new IllegalStateException("abc"));

		Future
			.fromCompletableFuture(result.toCompletableFuture())
			.await(3, TimeUnit.SECONDS);

		assertThat(result)
			.isCompletedExceptionally()
			.hasFailedWithThrowableThat()
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	public void test_askWithTimeout() throws Exception {

		final CompletionStage<String> result = selection.ask("STRING IS NOT RECEIVED");
		final ThrowingCallable response = () -> result.toCompletableFuture().get(1, TimeUnit.MILLISECONDS);

		assertThatThrownBy(response).isInstanceOf(TimeoutException.class);
	}
}
