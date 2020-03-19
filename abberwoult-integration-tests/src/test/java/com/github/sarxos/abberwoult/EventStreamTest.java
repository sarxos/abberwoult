package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.EventStreamTesting.Bobek;
import com.github.sarxos.abberwoult.EventStreamTesting.Mumin;
import com.github.sarxos.abberwoult.EventStreamTesting.Noone;
import com.github.sarxos.abberwoult.EventStreamTesting.Ready;
import com.github.sarxos.abberwoult.EventStreamTesting.EventStreamTestActor;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.event.EventStream;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class EventStreamTest {

	@Inject
	TestKit testkit;

	@Inject
	EventStream events;

	@Test
	public void test_eventsReceived() throws InterruptedException {

		final BeanManager bm = CDI.current().getBeanManager();

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(EventStreamTestActor.class)
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
