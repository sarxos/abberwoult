package com.github.sarxos.abberwoult;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSubMediator;


/**
 * A distributed pub-sub topic actors can subscribe to.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class Topic {

	private final String name;
	private final ActorRef mediator;

	public Topic(final String name, final ActorRef mediator) {
		this.name = name;
		this.mediator = mediator;
	}

	public void subscribe(final ActorRef subscriber) {
		mediator.tell(new DistributedPubSubMediator.Subscribe(name, subscriber), subscriber);
	}

	public void unsubscribe(final ActorRef subscriber) {
		mediator.tell(new DistributedPubSubMediator.Unsubscribe(name, subscriber), subscriber);
	}
}
