package com.github.sarxos.abberwoult.builder;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.ClusterCoordinator;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.dsl.Utils;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;
import io.quarkus.test.junit.QuarkusTest;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


@QuarkusTest
public class SingletonBuilderTest {

	public static class SingletonActor extends SimpleActor implements Utils {

		final AtomicBoolean started;
		final AtomicBoolean disposed;

		public SingletonActor(final AtomicBoolean started, final AtomicBoolean disposes) {
			this.started = started;
			this.disposed = disposes;
		}

		@PreStart
		public void setup() {
			started.set(true);
		}

		@PostStop
		public void teardown() {
			disposed.set(true);
		}
	}

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

	private Object askResult(final ActorRef ref, final Object message) throws Exception {
		final Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		final Future<Object> future = Patterns.ask(ref, message, timeout);
		return Await.result(future, timeout.duration());
	}
}
