package com.github.sarxos.abberwoult;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorSystemUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.Topic;
import com.github.sarxos.abberwoult.annotation.Assisted;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.PubSubs;
import com.github.sarxos.abberwoult.dsl.Utils;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class TopicTest {

	@Inject
	@Labeled("test")
	Topic topic;

	@Inject
	ActorSystemUniverse universe;

	@Inject
	TestKit testkit;

	public static class TestActor extends SimpleActor implements Utils, PubSubs {

		private final ActorRef ref;
		private final Topic topic;

		@Inject
		public TestActor(@Assisted TestKitProbe probe, @Labeled("test") Topic topic) {
			this.ref = probe.getRef();
			this.topic = topic;
		}

		@PreStart
		public void setup() {
			subscribe(topic);
		}

		public void onIntegerReceived(@Receives Integer i) {
			forward(ref, i);
		}

		@Override
		public void onSubscribeAck(@Receives SubscribeAck ack) {
			forward(ref, ack);
		}
	}

	@Test
	public void test_subscribe() {

		final TestKitProbe probe = testkit.probe();

		testkit.actor()
			.of(TestActor.class)
			.withArguments(probe)
			.create();

		final SubscribeAck ack = probe.expectMsgClass(SubscribeAck.class);

		Assertions
			.assertThat(ack)
			.isNotNull();
	}

	@Test
	public void test_publish() {

		final TestKitProbe probe = testkit.probe();

		testkit.actor()
			.of(TestActor.class)
			.withArguments(probe)
			.create();

		probe.expectMsgClass(SubscribeAck.class);

		topic.publish(234);

		final Integer value = probe.expectMsgClass(Integer.class);

		Assertions
			.assertThat(value)
			.isEqualTo(234);
	}
}
