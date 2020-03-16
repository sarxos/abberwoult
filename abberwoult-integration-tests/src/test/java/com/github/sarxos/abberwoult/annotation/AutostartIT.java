package com.github.sarxos.abberwoult.annotation;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.AskableActorSelection;
import com.github.sarxos.abberwoult.annotation.AutostartTesting.AutostartActor;
import com.github.sarxos.abberwoult.deployment.ActorAutostarter;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class AutostartIT {

	@Inject
	@ActorOf(AutostartActor.class)
	AskableActorSelection actor;

	@Inject
	ActorAutostarter registry;

	@Test
	public void test_isAutostartActorInRegistry() throws InterruptedException, ExecutionException {
		assertThat(registry.getRef(AutostartActor.class)).isNotNull();
	}

	@Test
	public void test_autostartActor() throws InterruptedException, ExecutionException {

		final CompletableFuture<Object> future = actor
			.ask(1)
			.toCompletableFuture();

		// We receive response because actor is already started since it's annotated with
		// Autostart annotation.

		await().until(future::isDone);
		assertThat(future.get()).isEqualTo(1);
		dispose(actor);
	}
}
