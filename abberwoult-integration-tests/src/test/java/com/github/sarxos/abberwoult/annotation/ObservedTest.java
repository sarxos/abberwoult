package com.github.sarxos.abberwoult.annotation;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorSystemUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;
import com.github.sarxos.abberwoult.trait.Utils;

import akka.actor.ActorRef;
import akka.event.EventStream;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ObservedTest {

	public static interface Something {
	}

	public static class SomethingImpl implements Something {
	}

	public static class TestActor extends SimpleActor implements Utils {

		private final ActorRef ref;

		public TestActor(final TestKitProbe probe) {
			this.ref = probe.getRef();
		}

		public void handleInteger(@Observes Integer i) {
			forward(ref, i);
		}

		public void handleLong(@Receives @Observes Long l) {
			forward(ref, l);
		}

		public void handleSomething(@Receives @Observes Something s) {
			forward(ref, s);
		}
	}

	@Inject
	TestKit testkit;

	@Inject
	EventStream events;

	@Inject
	ActorSystemUniverse universe;

	TestKitProbe probe;

	ActorRef ref;

	@BeforeEach
	public void setup() {

		probe = testkit.probe();

		ref = testkit.actor()
			.of(TestActor.class)
			.withArguments(probe)
			.create();

		testkit.awaitForActor(ref);
	}

	@AfterEach
	public void teardown() {
		testkit.kill(ref);
	}

	@Test
	public void test_observedOneReceiver() {

		events.publish(1);
		events.publish(2);
		events.publish(3);

		probe.expectMsg(1);
		probe.expectMsg(2);
		probe.expectMsg(3);
	}

	@Test
	public void test_observedManyReceiver() {

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
	public void test_observedInterfaceClass() {

		events.publish(new SomethingImpl());

		probe.expectMsgClass(Something.class);
	}
}
