package com.github.sarxos.abberwoult.deployment;

import static akka.actor.ActorRef.noSender;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorSystemUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.annotation.PreStart;

import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ActorAutostarterTest {

	private static final AtomicBoolean started = new AtomicBoolean(false);

	@Autostart
	@Labeled("autostartedactor")
	public static class TestActor extends SimpleActor {

		@PreStart
		public void setup() {
			started.set(true);
		}
	}

	@Inject
	ActorSystemUniverse universe;

	@Test
	public void test_autostart() {
		await().untilTrue(started);
	}

	@AfterEach
	public void teardown() {
		universe
			.select(TestActor.class)
			.tell(PoisonPill.getInstance(), noSender());
	}
}
