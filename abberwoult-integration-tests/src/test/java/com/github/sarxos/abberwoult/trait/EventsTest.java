package com.github.sarxos.abberwoult.trait;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.Coupler;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class EventsTest {

	static class WatchMsg {
	}

	public static class WatchedActor extends SimpleActor {

	}

	public static class WatchingActor extends SimpleActor implements Watcher, Comm {

		private final AtomicBoolean terminated;
		private ActorRef watched;

		public WatchingActor(final AtomicBoolean terminated) {
			this.terminated = terminated;
		}

		@PreStart
		public void setup() {
			this.watched = actor().of(WatchedActor.class).build();
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
	Coupler coupler;

	@Inject
	TestKit testkit;

	@Test
	public void test_watch() {

		final AtomicBoolean terminated = new AtomicBoolean();

		final ActorRef watcher = testkit.actor()
			.of(WatchingActor.class)
			.withArguments(terminated)
			.build();

		final WatchingActor actor = testkit.extractReady(watcher);

		assertThat(actor).isNotNull();
		assertThat(actor.watched).isNotNull();
		assertThat(actor.terminated).isFalse();

		watcher.tell(new WatchMsg(), ActorRef.noSender());
		actor.watched.tell(new WatchMsg(), ActorRef.noSender());

		await().untilTrue(actor.terminated);
	}

}
