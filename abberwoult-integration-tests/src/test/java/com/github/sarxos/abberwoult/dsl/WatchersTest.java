package com.github.sarxos.abberwoult.dsl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Utils;
import com.github.sarxos.abberwoult.dsl.Watchers;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class WatchersTest {

	static class WatchMsg {
	}

	public static class WatchedActor extends SimpleActor {

	}

	public static class WatchingActor extends SimpleActor implements Watchers, Utils {

		private final AtomicBoolean terminated;
		private ActorRef watched;

		public WatchingActor(final AtomicBoolean terminated) {
			this.terminated = terminated;
		}

		@PreStart
		public void setup() {
			this.watched = actor().of(WatchedActor.class).create();
		}

		@Override
		public void onActorTerminated(final ActorRef ref) {
			if (ActorUtils.equals(watched, ref)) {
				terminated.set(true);
			}
		}

		public void handleWatchMsg(@Receives WatchMsg msg) {
			watch(watched);
		}
	}

	@Inject
	ActorSystem system;

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
