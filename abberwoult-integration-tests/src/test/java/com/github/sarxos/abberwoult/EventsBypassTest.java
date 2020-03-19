package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.EventsBypassTesting.Event1;
import com.github.sarxos.abberwoult.EventsBypassTesting.Event2;
import com.github.sarxos.abberwoult.EventsBypassTesting.Event3;
import com.github.sarxos.abberwoult.EventsBypassTesting.EventBypassTestActor;
import com.github.sarxos.abberwoult.EventsBypassTesting.Ready;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class EventsBypassTest {

	@Inject
	TestKit testkit;

	@Inject
	BeanManager bm;

	@Test
	public void test_eventsReceived() throws InterruptedException {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(EventBypassTestActor.class)
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
