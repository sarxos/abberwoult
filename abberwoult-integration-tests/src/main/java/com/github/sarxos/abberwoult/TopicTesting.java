package com.github.sarxos.abberwoult;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.annotation.Assisted;
import com.github.sarxos.abberwoult.annotation.NamedActor;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.PubSubs;
import com.github.sarxos.abberwoult.dsl.Utils;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;


public class TopicTesting {

	public static class TopicTestActor extends SimpleActor implements Utils, PubSubs {

		private final ActorRef ref;
		private final Topic topic;

		@Inject
		public TopicTestActor(@Assisted TestKitProbe probe, @NamedActor("test") Topic topic) {
			this.ref = probe.getRef();
			this.topic = topic;
		}

		@PreStart
		public void setup() {
			subscribe(topic);
		}

		public void onIntegerReceived(@Receives Integer i) {
			forward(i, ref);
		}

		@Override
		public void onSubscribeAck(@Receives SubscribeAck ack) {
			forward(ack, ref);
		}
	}
}
