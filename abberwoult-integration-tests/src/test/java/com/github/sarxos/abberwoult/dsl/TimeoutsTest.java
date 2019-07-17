package com.github.sarxos.abberwoult.dsl;

import static com.github.sarxos.abberwoult.util.ActorUtils.dispose;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Timeouts;
import com.github.sarxos.abberwoult.dsl.Utils;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class TimeoutsTest {

	public static final class TimeoutingActor extends SimpleActor implements Timeouts, Utils {

		private final ActorRef ref;

		public TimeoutingActor(final TestKitProbe probe) {
			this.ref = probe.getRef();
		}

		@Override
		public int getReceiveTimeout() {
			return 1; // 1 second
		}

		@Override
		public void onReceiveTimeout(@Receives final ReceiveTimeout timeout) {
			forward(ref, timeout);
		}
	}

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
