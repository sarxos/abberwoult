package com.github.sarxos.abberwoult.dsl;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.dsl.TimeoutsTesting.TimeoutingActor;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class TimeoutsTest {

	@Inject
	TestKit testkit;

	@Test
	public void test_receiveTimeout() throws InterruptedException {

		final TestKitProbe probe = testkit.probe();

		final ActorRef ref = testkit.actor()
			.of(TimeoutingActor.class)
			.withArguments(probe)
			.create();

		probe.expectMsgClass(ReceiveTimeout.class);

		dispose(ref);
	}
}
