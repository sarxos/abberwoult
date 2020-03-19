package com.github.sarxos.abberwoult;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.TopicTesting.TopicTestActor;
import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.testkit.TestKit;
import com.github.sarxos.abberwoult.testkit.TestKitProbe;

import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class TopicTest {

	@Inject
	@Named("test")
	Topic topic;

	@Inject
	ActorUniverse universe;

	@Inject
	TestKit testkit;

	@Test
	public void test_subscribe() {

		final TestKitProbe probe = testkit.probe();

		testkit.actor()
			.of(TopicTestActor.class)
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
			.of(TopicTestActor.class)
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
