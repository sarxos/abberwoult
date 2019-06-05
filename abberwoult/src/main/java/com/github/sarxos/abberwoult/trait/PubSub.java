package com.github.sarxos.abberwoult.trait;

import com.github.sarxos.abberwoult.Topic;
import com.github.sarxos.abberwoult.annotation.Received;

import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;


public interface PubSub extends ActorInternal {

	/**
	 * Subscribes this actor to the given distributed pub-sub topic.
	 *
	 * @param topic a topic to be subscribed
	 */
	default void subscribe(final Topic topic) {
		topic.subscribe(self());
	}

	/**
	 * Unsubscribes this actor from the given distributed pub-sub topic.
	 *
	 * @param topic a topic to be unsubscribed
	 */
	default void unsubscribe(final Topic topic) {
		topic.unsubscribe(self());
	}

	/**
	 * Obligatory massage handler for {@link SubscribeAck} which is replied to the actor after
	 * {@link Topic} is subscribed.
	 *
	 * @param ack the subscription acknowledgement
	 */
	void onSubscribeAck(@Received SubscribeAck ack);

}
