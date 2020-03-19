package com.github.sarxos.abberwoult.dsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.dsl.WatchersTesting.WatchMsg;
import com.github.sarxos.abberwoult.dsl.WatchersTesting.WatchingActor;
import com.github.sarxos.abberwoult.testkit.TestKit;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class WatchersTest {

	@Inject
	TestKit testkit;

	@Test
	public void test_watch() throws InterruptedException {

		final AtomicBoolean terminated = new AtomicBoolean();

		final ActorRef watcher = testkit.actor()
			.of(WatchingActor.class)
			.withArguments(terminated)
			.create();

		final WatchingActor actor = testkit.extractReady(watcher);

		assertThat(actor).isNotNull();
		assertThat(actor.watched).isNotNull();
		assertThat(actor.terminated).isFalse();

		watcher.tell(new WatchMsg(), ActorRef.noSender());

		actor.watched.tell(PoisonPill.getInstance(), ActorRef.noSender());

		await().untilTrue(actor.terminated);
	}

}
