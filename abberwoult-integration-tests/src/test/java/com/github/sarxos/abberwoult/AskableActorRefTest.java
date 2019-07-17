package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.AskableActorRef;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Disposers;
import com.github.sarxos.abberwoult.dsl.Utils;

import io.quarkus.test.junit.QuarkusTest;
import io.vavr.concurrent.Future;


@QuarkusTest
public class AskableActorRefTest {

	@Inject
	@ActorOf(TestClass.class)
	AskableActorRef ref;

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

	@Test
	public void test_ask() throws Exception {

		final CompletionStage<Integer> result = ref.ask(22);
		final Integer i = result.toCompletableFuture().get(3, TimeUnit.SECONDS);

		assertThat(i).isEqualTo(22);
	}

	@Test
	public void test_askWithException() throws Exception {

		final CompletionStage<String> result = ref.ask(new IllegalStateException("abc"));

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

		final CompletionStage<String> result = ref.ask("STRING IS NOT RECEIVED");
		final ThrowingCallable response = () -> result.toCompletableFuture().get(1, TimeUnit.MILLISECONDS);

		assertThatThrownBy(response).isInstanceOf(TimeoutException.class);
	}
}
