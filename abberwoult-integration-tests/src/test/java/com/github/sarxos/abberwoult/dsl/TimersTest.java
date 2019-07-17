package com.github.sarxos.abberwoult.dsl;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;

import java.time.Duration;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class TimersTest {

	@Inject
	TestKit testkit;

	public static class Tick {
	}

	@Test
	public void test_scheduleOnce() {

		class TestActor extends SimpleActor implements Timers, Utils {

			final ActorRef probe;

			@SuppressWarnings("unused")
			public TestActor(final TestKitProbe probe) {
				this.probe = probe.getRef();
			}

			@PreStart
			public void setup() {
				scheduleOnce("bubu", new Tick(), Duration.ofMillis(10));
			}

			@SuppressWarnings("unused")
			public void onTick(@Receives Tick tick) {
				forward(probe, tick);
			}
		}

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor.class)
			.withArguments(this, probe)
			.create();

		probe.expectMsgClass(Tick.class);
		probe.expectNoMessage(Duration.ofMillis(100));

		dispose(ref);
	}

	@Test
	public void test_scheduleOnceWithRecipient() {

		class TestRecipientActor extends SimpleActor implements Timers, Utils {

			final ActorRef probe;

			@SuppressWarnings("unused")
			public TestRecipientActor(final TestKitProbe probe) {
				this.probe = probe.getRef();
			}

			@SuppressWarnings("unused")
			public void onTick(@Receives Tick tick) {
				forward(probe, tick);
			}
		}

		class TestSchedulerActor extends SimpleActor implements Timers, Utils {

			final ActorRef recipient;

			@SuppressWarnings("unused")
			public TestSchedulerActor(final ActorRef recipient) {
				this.recipient = recipient;
			}

			@PreStart
			public void setup() {
				scheduleOnce("bubu", new Tick(), recipient, Duration.ofMillis(10));
			}
		}

		final TestKitProbe probe = testkit.probe();

		final ActorRef recipient = testkit.actor()
			.of(TestRecipientActor.class)
			.withArguments(this, probe)
			.create();

		final ActorRef scheduler = testkit.actor()
			.of(TestSchedulerActor.class)
			.withArguments(this, recipient)
			.create();

		probe.expectMsgClass(Tick.class);
		probe.expectNoMessage(Duration.ofMillis(100));

		dispose(recipient);
		dispose(scheduler);
	}

}
