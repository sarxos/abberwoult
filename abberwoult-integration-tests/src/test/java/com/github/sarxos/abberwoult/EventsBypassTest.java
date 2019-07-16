package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;
import com.github.sarxos.abberwoult.trait.Utils;
import com.github.sarxos.abberwoult.trait.Events;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class EventsBypassTest {

	static class Ready {
	}

	static class Event1 {
	}

	static class Event2 {
	}

	static class Event3 {
	}

	@Inject
	TestKit testkit;

	public static final class TestActor extends SimpleActor implements Events, Utils {

		private final ActorRef probe;

		public TestActor(final ActorRef probe) {
			this.probe = probe;
		}

		@PreStart
		public void setup() {
			eventSubscribe(Event1.class);
			eventSubscribe(Event2.class);
		}

		public void handleReady(@Receives Ready msg) {
			forward(probe, msg);
		}

		public void handleEvent1(@Receives Event1 event) {
			forward(probe, event);
		}

		public void handleEvent2(@Receives Event2 event) {
			forward(probe, event);
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

		bm.fireEvent(new Event3()); // should NOT be received
		bm.fireEvent(new Event2()); // should be received
		bm.fireEvent(new Event1()); // should be received

		probe.expectMsgClass(Event2.class);
		probe.expectMsgClass(Event1.class);
		probe.expectNoMessage();
	}
}
