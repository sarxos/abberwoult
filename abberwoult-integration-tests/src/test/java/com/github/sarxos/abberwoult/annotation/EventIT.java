package com.github.sarxos.abberwoult.annotation;

import java.time.Duration;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.event.EventStream;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class EventIT {

	@Inject
	TestKit testkit;

	@Inject
	EventStream events;

	@Inject
	ActorUniverse universe;

	TestKitProbe probe;

	ActorRef ref;

	@BeforeEach
	public void setup() {

		probe = testkit.probe();

		ref = testkit.actor()
			.of(EventActor.class)
			.withArguments(probe)
			.create();

		testkit.awaitForActor(ref);
	}

	@AfterEach
	public void teardown() {
		testkit.kill(ref);
	}

	@Test
	public void test_eventOneReceiver() {

		events.publish(1);
		events.publish(2);
		events.publish(3);

		probe.expectMsg(1);
		probe.expectMsg(2);
		probe.expectMsg(3);
	}

	@Test
	public void test_eventValid() {

		final EventActor.Book b = new EventActor.Book("abbabba");

		events.publish(b);

		probe.expectMsg(b);
	}

	@Test
	public void test_eventInvalid() {

		final EventActor.Book b = new EventActor.Book("a");

		events.publish(b);

		probe.expectNoMessage(Duration.ofSeconds(1));
	}

	@Test
	public void test_eventManyReceiver() {

		events.publish(1);
		events.publish(2);
		events.publish(3);

		events.publish(5L);
		events.publish(6L);
		events.publish(7L);

		probe.expectMsg(1);
		probe.expectMsg(2);
		probe.expectMsg(3);

		probe.expectMsg(5L);
		probe.expectMsg(6L);
		probe.expectMsg(7L);
	}

	@Test
	public void test_eventInterfaceClass() {

		events.publish(new EventActor.SomeImpl());

		probe.expectMsgClass(EventActor.Some.class);
	}
}
