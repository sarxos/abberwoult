package com.github.sarxos.abberwoult.annotation;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.AskableActorSelection;
import com.github.sarxos.abberwoult.annotation.AutostartTesting.AutostartActor;
import com.github.sarxos.abberwoult.deployment.ActorAutostarter;

import akka.actor.Identify;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class AutostartTest {

	@Inject
	@ActorOf(AutostartActor.class)
	AskableActorSelection actor;

	@Inject
	ActorAutostarter registry;

	@BeforeEach
	public void await() throws InterruptedException, ExecutionException, TimeoutException {
		actor
			.ask(new Identify(1))
			.toCompletableFuture()
			.get(3, TimeUnit.SECONDS);
	}

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

		Awaitility.await().until(future::isDone);

		assertThat(future.get()).isEqualTo(1);
		dispose(actor);
	}
}
