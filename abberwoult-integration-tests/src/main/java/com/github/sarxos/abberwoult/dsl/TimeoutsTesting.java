package com.github.sarxos.abberwoult.dsl;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.actor.ReceiveTimeout;


public class TimeoutsTesting {

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
		public void onReceiveTimeout(@Received final ReceiveTimeout timeout) {
			forward(timeout, ref);
		}
	}
}
