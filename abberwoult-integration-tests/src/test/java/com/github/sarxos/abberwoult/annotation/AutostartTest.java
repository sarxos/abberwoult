package com.github.sarxos.abberwoult.annotation;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.AskableActorSelection;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class AutostartTest {

	@Autostart
	@Named("testactorstart")
	public static class TestActorAutostart extends SimpleActor implements Utils {
		public void onInteger(@Receives Integer i) {
			reply(i);
		}
	}

	@Inject
	@ActorOf(TestActorAutostart.class)
	AskableActorSelection askable;

	@Test
	public void test_autostart() throws InterruptedException, ExecutionException {

		final CompletableFuture<Object> future = askable
			.ask(1)
			.toCompletableFuture();

		// We should get response because actor should already be started since it's annotated with
		// Autostart annotation.

		await().until(future::isDone);
		assertThat(future.get()).isEqualTo(1);
		dispose(askable);
	}
}
