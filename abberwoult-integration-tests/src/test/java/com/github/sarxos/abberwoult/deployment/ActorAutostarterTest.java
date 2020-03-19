package com.github.sarxos.abberwoult.deployment;

import static akka.actor.ActorRef.noSender;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;

import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ActorAutostarterTest {

	@Inject
	ActorUniverse universe;

	@Test
	public void test_autostart() {
		final AtomicInteger started = ActorAutostarterTestActor.started;
		try {
			await().untilAtomic(started, is(1));
		} finally {
			universe
				.select(ActorAutostarterTestActor.class)
				.tell(PoisonPill.getInstance(), noSender());
		}
	}
}
