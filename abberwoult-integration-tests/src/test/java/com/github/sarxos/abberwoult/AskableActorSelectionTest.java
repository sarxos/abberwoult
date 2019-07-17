package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Disposers;
import com.github.sarxos.abberwoult.dsl.Utils;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Terminated;
import io.quarkus.test.junit.QuarkusTest;
import io.vavr.concurrent.Future;


@QuarkusTest
public class AskableActorSelectionTest {

	@Inject
	ActorSystemUniverse universe;

	@Inject
	@ActorOf(TestClass.class)
	AskableActorSelection selection;

	@Inject
	TestKit testkit;

	ActorRef ref;
	
	@Labeled("test")
	public static class TestClass extends SimpleActor implements Utils, Disposers {

		public void handleInteger(@Receives final Integer i) {
			replyAndDispose(i);
		}

		public void handleThrowable(@Receives final Throwable t) {
			replyAndDispose(t);
		}

		private void replyAndDispose(final Object value) {
			reply(value);
			dispose();
		}
	}

	@BeforeEach
	public void setup() {
		ref = testkit.actor()
			.of(TestClass.class)
			.create();
	}

	@AfterEach
	public void teardown() {
		final TestKitProbe probe =  testkit.probe();
		probe.watch(ref);
		selection.tell(PoisonPill.getInstance());
		probe.expectMsgClass(Terminated.class);
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
