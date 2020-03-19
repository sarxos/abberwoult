package com.github.sarxos.abberwoult.builder;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.ClusterCoordinator;
import com.github.sarxos.abberwoult.builder.SingletonBuilderTesting.SingletonActor;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class SingletonBuilderTest {

	@Inject
	ActorUniverse universe;

	@Inject
	ClusterCoordinator coordinator;

	@BeforeEach
	void bootstrap() {
		coordinator.bootstrap();
	}

	@Test
	void test_lifecycle() throws Exception {

		final AtomicBoolean started = new AtomicBoolean(false);
		final AtomicBoolean disposed = new AtomicBoolean(false);

		final ActorRef ref = universe.singleton()
			.of(SingletonActor.class)
			.withArguments(started, disposed)
			.create();

		await().untilTrue(started);

		dispose(ref);

		await().untilTrue(disposed);
	}
}
