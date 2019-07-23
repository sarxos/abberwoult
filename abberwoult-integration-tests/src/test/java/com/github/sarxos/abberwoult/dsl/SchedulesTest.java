package com.github.sarxos.abberwoult.dsl;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Schedules.Schedule;
import com.github.sarxos.abberwoult.dsl.Schedules.ScheduleCancelMsg;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class SchedulesTest {

	@Inject
	TestKit testkit;

	public static class Tick {
	}

	@Test
	public void test_scheduleOnce() {

		class TestActor extends SimpleActor implements Schedules, Utils {

			final Map<String, Schedule> schedules = new HashMap<>(1);
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

			@Override
			public Map<String, Schedule> getSchedules() {
				return schedules;
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
	public void test_scheduleOnceCancelByMessage() {

		class TestActor extends SimpleActor implements Schedules, Utils {

			final Map<String, Schedule> schedules = new HashMap<>(1);
			final ActorRef probe;

			@SuppressWarnings("unused")
			public TestActor(final TestKitProbe probe) {
				this.probe = probe.getRef();
			}

			@PreStart
			public void setup() {
				scheduleOnce("bubu", new Tick(), Duration.ofMillis(5000));
			}

			@Override
			public void onScheduleCancelMsgAck(@Receives ScheduleCancelMsg.Ack ack) {
				forward(probe, ack);
			}

			@Override
			public Map<String, Schedule> getSchedules() {
				return schedules;
			}
		}

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor.class)
			.withArguments(this, probe)
			.create();

		final TestActor actor = testkit.extractReady(ref);

		Assertions
			.assertThat(actor.getSchedules())
			.hasSize(1);

		ref.tell(new ScheduleCancelMsg("bubu"), ref);

		probe.expectMsgClass(ScheduleCancelMsg.Ack.class);

		Assertions
			.assertThat(actor.getSchedules())
			.isEmpty();

		dispose(ref);
	}

	@Test
	public void test_scheduleOnceRemovedWhenDone() {

		class TestActor extends SimpleActor implements Schedules, Utils {

			final Map<String, Schedule> schedules = new HashMap<>(1);
			final ActorRef probe;

			@SuppressWarnings("unused")
			public TestActor(final TestKitProbe probe) {
				this.probe = probe.getRef();
			}

			@PreStart
			public void setup() {
				scheduleOnce("bubu", new Tick(), Duration.ofMillis(5));
			}

			@SuppressWarnings("unused")
			public void onTick(@Receives Tick tick) {
				forward(probe, tick);
			}

			@Override
			public void onScheduleRemoveMsg(@Receives ScheduleRemoveMsg msg) {
				Schedules.super.onScheduleRemoveMsg(msg);
				forward(probe, msg);
			}

			@Override
			public Map<String, Schedule> getSchedules() {
				return schedules;
			}
		}

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor.class)
			.withArguments(this, probe)
			.create();

		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(ScheduleRemoveMsg.class);

		final TestActor actor = testkit.extractReady(ref);

		Assertions
			.assertThat(actor.getSchedules())
			.isEmpty();

		dispose(ref);
	}

	@Test
	public void test_scheduleOnceWithRecipient() {

		class TestRecipientActor extends SimpleActor implements Utils {

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

		class TestSchedulerActor extends SimpleActor implements Schedules, Utils {

			final Map<String, Schedule> schedules = new HashMap<>(1);
			final ActorRef recipient;

			@SuppressWarnings("unused")
			public TestSchedulerActor(final ActorRef recipient) {
				this.recipient = recipient;
			}

			@PreStart
			public void setup() {
				scheduleOnce("bubu", new Tick(), recipient, Duration.ofMillis(10));
			}

			@Override
			public Map<String, Schedule> getSchedules() {
				return schedules;
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

	@Test
	public void test_schedule() {

		class TestActor extends SimpleActor implements Schedules, Utils {

			final Map<String, Schedule> schedules = new HashMap<>(1);
			final ActorRef probe;

			@SuppressWarnings("unused")
			public TestActor(final TestKitProbe probe) {
				this.probe = probe.getRef();
			}

			@PreStart
			public void setup() {
				schedule("bubu", new Tick(), Duration.ofMillis(10));
			}

			@SuppressWarnings("unused")
			public void onTick(@Receives Tick tick) {
				forward(probe, tick);
			}

			@Override
			public Map<String, Schedule> getSchedules() {
				return schedules;
			}
		}

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor.class)
			.withArguments(this, probe)
			.create();

		probe.watch(ref);

		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);

		final TestActor actor = testkit.extract(ref);

		Assertions
			.assertThat(actor.getSchedules())
			.hasSize(1)
			.containsKey("bubu");

		final Schedule schedule = actor.getSchedules().get("bubu");

		Assertions
			.assertThat(schedule.getName())
			.isEqualTo("bubu");

		Assertions
			.assertThat(schedule.isCancelled())
			.isFalse();

		dispose(ref);

		probe.expectTerminated(ref);

		Assertions
			.assertThat(actor.getSchedules())
			.isEmpty();

		Assertions
			.assertThat(schedule.isCancelled())
			.isTrue();
	}

	@Test
	public void test_scheduleCancelRemovesItFromMap() {

		class TestActor extends SimpleActor implements Schedules, Utils {

			final Map<String, Schedule> schedules = new HashMap<>(1);
			final ActorRef probe;

			@SuppressWarnings("unused")
			public TestActor(final TestKitProbe probe) {
				this.probe = probe.getRef();
			}

			@PreStart
			public void setup() {
				schedule("bubu", new Tick(), Duration.ofMillis(10));
			}

			@SuppressWarnings("unused")
			public void onTick(@Receives Tick tick) {
				forward(probe, tick);
			}

			@Override
			public void onScheduleRemoveMsg(@Receives ScheduleRemoveMsg msg) {
				Schedules.super.onScheduleRemoveMsg(msg);
				forward(probe, msg);
			}

			@Override
			public Map<String, Schedule> getSchedules() {
				return schedules;
			}
		}

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor.class)
			.withArguments(this, probe)
			.create();

		probe.expectMsgClass(Tick.class);

		final TestActor actor = testkit.extract(ref);
		final Schedule schedule = actor.getSchedules().get("bubu");
		final boolean cancelled = schedule.cancel();

		Assertions
			.assertThat(cancelled)
			.isTrue();

		final ScheduleRemoveMsg msg = probe.expectMsgClass(ScheduleRemoveMsg.class);

		Assertions
			.assertThat(msg.getName())
			.isEqualTo("bubu");

		Assertions
			.assertThat(actor.getSchedules())
			.isEmpty();

		dispose(ref);
	}
}
