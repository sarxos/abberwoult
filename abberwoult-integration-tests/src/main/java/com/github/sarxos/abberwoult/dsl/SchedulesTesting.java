package com.github.sarxos.abberwoult.dsl;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;


public class SchedulesTesting {

	public static class Tick {
	}

	public static class TestActor1 extends SimpleActor implements Schedules, Utils {

		final Map<String, Schedule> schedules = new HashMap<>(1);
		final ActorRef probe;

		public TestActor1(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void setup() {
			scheduleOnce("bubu", new Tick(), Duration.ofMillis(10));
		}

		public void onTick(@Received Tick tick) {
			forward(tick, probe);
		}

		@Override
		public Map<String, Schedule> getSchedules() {
			return schedules;
		}
	}

	public static class TestActor2 extends SimpleActor implements Schedules, Utils {

		final Map<String, Schedule> schedules = new HashMap<>(1);
		final ActorRef probe;

		public TestActor2(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void setup() {
			scheduleOnce("bubu", new Tick(), Duration.ofMillis(5000));
		}

		@Override
		public void onScheduleCancelMsgAck(@Received ScheduleCancelMsg.Ack ack) {
			forward(ack, probe);
		}

		@Override
		public Map<String, Schedule> getSchedules() {
			return schedules;
		}
	}

	public static class TestActor3 extends SimpleActor implements Schedules, Utils {

		final Map<String, Schedule> schedules = new HashMap<>(1);
		final ActorRef probe;

		public TestActor3(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void setup() {
			scheduleOnce("bubu", new Tick(), Duration.ofMillis(5));
		}

		public void onTick(@Received Tick tick) {
			forward(tick, probe);
		}

		@Override
		public void onScheduleRemoveMsg(@Received ScheduleRemoveMsg msg) {
			Schedules.super.onScheduleRemoveMsg(msg);
			forward(msg, probe);
		}

		@Override
		public Map<String, Schedule> getSchedules() {
			return schedules;
		}
	}

	public static class TestRecipientActor extends SimpleActor implements Utils {

		final ActorRef probe;

		public TestRecipientActor(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		public void onTick(@Received Tick tick) {
			forward(tick, probe);
		}
	}

	public static class TestSchedulerActor extends SimpleActor implements Schedules, Utils {

		final Map<String, Schedule> schedules = new HashMap<>(1);
		final ActorRef recipient;

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

	public static class TestActor4 extends SimpleActor implements Schedules, Utils {

		final Map<String, Schedule> schedules = new HashMap<>(1);
		final ActorRef probe;

		public TestActor4(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void setup() {
			schedule("bubu", new Tick(), Duration.ofMillis(10));
		}

		public void onTick(@Received Tick tick) {
			forward(tick, probe);
		}

		@Override
		public Map<String, Schedule> getSchedules() {
			return schedules;
		}
	}

	public static class TestActor5 extends SimpleActor implements Schedules, Utils {

		final Map<String, Schedule> schedules = new HashMap<>(1);
		final ActorRef probe;

		public TestActor5(final TestKitProbe probe) {
			this.probe = probe.getRef();
		}

		@PreStart
		public void setup() {
			schedule("bubu", new Tick(), Duration.ofMillis(10));
		}

		public void onTick(@Received Tick tick) {
			forward(tick, probe);
		}

		@Override
		public Map<String, Schedule> getSchedules() {
			return schedules;
		}
	}
}
