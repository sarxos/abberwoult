package com.github.sarxos.abberwoult.builder;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
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

		public SingletonActor(final AtomicBoolean started) {
			this.started = started;
		}

		@PreStart
		public void setup() {
			started.set(true);
		}
	}

	@Inject
	ActorUniverse universe;

	@Test
	@Disabled("Not ready yet, cluster singleton need to be implemented")
	void test_() throws Exception {

		final AtomicBoolean started = new AtomicBoolean(false);

		final ActorRef ref = universe.singleton()
			.of(SingletonActor.class)
			.withArguments(started)
			.create();

		await().untilTrue(started);

		dispose(ref);
	}

	private Object askResult(final ActorRef ref, final Object message) throws Exception {
		final Timeout timeout = new Timeout(Duration.create(5, "seconds"));
		final Future<Object> future = Patterns.ask(ref, message, timeout);
		return Await.result(future, timeout.duration());
	}
}
