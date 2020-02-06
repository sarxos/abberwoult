package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.Event;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Events;
import com.github.sarxos.abberwoult.dsl.Utils;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.event.EventStream;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class EventStreamTest {

	static class Ready {
	}

	static class Bobek {
	}

	static class Mumin {
	}

	static class Noone {
	}

	@Inject
	TestKit testkit;

	@Inject
	EventStream events;

	public static final class TestActor extends SimpleActor implements Events, Utils {

		private final ActorRef probe;

		public TestActor(final ActorRef probe) {
			this.probe = probe;
		}

		public void handleReady(@Receives Ready msg) {
			forward(probe, msg);
		}

		public void handleEvent1(@Receives @Event Bobek b) {
			forward(probe, b);
		}

		public void handleEvent2(@Receives @Event Mumin m) {
			forward(probe, m);
		}
	}

	@Test
	public void test_eventsReceived() throws InterruptedException {

		final BeanManager bm = CDI.current().getBeanManager();

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TestActor.class)
			.withArguments(probe.getRef())
			.create();

		ref.tell(new Ready(), noSender());

		probe.expectMsgClass(Ready.class);

		bm.fireEvent(new Noone()); // should NOT be received
		bm.fireEvent(new Mumin()); // should be received
		bm.fireEvent(new Bobek()); // should be received

		probe.expectMsgClass(Mumin.class);
		probe.expectMsgClass(Bobek.class);
		probe.expectNoMessage();
	}
}
