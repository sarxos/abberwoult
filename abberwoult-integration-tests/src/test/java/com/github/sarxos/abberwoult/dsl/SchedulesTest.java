package com.github.sarxos.abberwoult.dsl;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;

import java.time.Duration;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.dsl.Schedules.Schedule;
import com.github.sarxos.abberwoult.dsl.Schedules.ScheduleCancelMsg;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.TestActor1;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.TestActor2;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.TestActor3;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.TestActor4;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.TestActor5;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.TestRecipientActor;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.TestSchedulerActor;
import com.github.sarxos.abberwoult.dsl.SchedulesTesting.Tick;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class SchedulesTest {

	@Inject
	TestKit testkit;

	@Test
	public void test_scheduleOnce() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor1.class)
			.withArguments(probe)
			.create();

		probe.expectMsgClass(Tick.class);
		probe.expectNoMessage(Duration.ofMillis(100));

		dispose(ref);
	}

	@Test
	public void test_scheduleOnceCancelByMessage() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor2.class)
			.withArguments(probe)
			.create();

		final TestActor2 actor = testkit.extractReady(ref);

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

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor3.class)
			.withArguments(probe)
			.create();

		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(ScheduleRemoveMsg.class);

		final TestActor3 actor = testkit.extractReady(ref);

		Assertions
			.assertThat(actor.getSchedules())
			.isEmpty();

		dispose(ref);
	}

	@Test
	public void test_scheduleOnceWithRecipient() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef recipient = testkit.actor()
			.of(TestRecipientActor.class)
			.withArguments(probe)
			.create();

		final ActorRef scheduler = testkit.actor()
			.of(TestSchedulerActor.class)
			.withArguments(recipient)
			.create();

		probe.expectMsgClass(Tick.class);
		probe.expectNoMessage(Duration.ofMillis(100));

		dispose(recipient);
		dispose(scheduler);
	}

	@Test
	public void test_schedule() {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor4.class)
			.withArguments(probe)
			.create();

		probe.watch(ref);

		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);
		probe.expectMsgClass(Tick.class);

		final TestActor4 actor = testkit.extract(ref);

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

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor5.class)
			.withArguments(probe)
			.create();

		probe.expectMsgClass(Tick.class);

		final TestActor5 actor = testkit.extract(ref);
		final Schedule schedule = actor.getSchedules().get("bubu");
		final boolean cancelled = schedule.cancel();

		Assertions
			.assertThat(cancelled)
			.isTrue();

		Assertions
			.assertThat(actor.getSchedules())
			.isEmpty();

		dispose(ref);
	}
}
