package com.github.sarxos.abberwoult;

import static akka.actor.ActorRef.noSender;

import java.io.Serializable;

import javax.enterprise.context.Dependent;

import com.github.sarxos.abberwoult.annotation.Labeled;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSubMediator.Publish;
import akka.cluster.pubsub.DistributedPubSubMediator.Subscribe;
import akka.cluster.pubsub.DistributedPubSubMediator.Unsubscribe;


/**
 * A distributed pub-sub topic actors can subscribe to. This class is meant to be injectable.
 * It can be used inside of actors as well as in the application context. The topic must be
 * annotated with {@link Labeled} qualifier to be injected. It's a {@link Dependent} scope
 * and created by {@link TopicFactory}.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class Topic {

	private final String name;
	private final ActorRef mediator;

	/**
	 * @param name a topic name (label)
	 * @param mediator a pub-sub mediator from {@link ActorSystem}
	 */
	Topic(final String name, final ActorRef mediator) {
		this.name = name;
		this.mediator = mediator;
	}

	/**
	 * Subscribe actor referenced by the given {@link ActorRef} to this topic.
	 *
	 * @param subscriber the subscriber's {@link ActorRef}
	 */
	public void subscribe(final ActorRef subscriber) {
		mediator.tell(new Subscribe(name, subscriber), subscriber);
	}

	/**
	 * Unsubscribe actor referenced by the given {@link ActorRef} from this topic.
	 *
	 * @param subscriber the subscriber's {@link ActorRef}
	 */
	public void unsubscribe(final ActorRef subscriber) {
		mediator.tell(new Unsubscribe(name, subscriber), subscriber);
	}

	/**
	 * Publish message to this topic. Message will be published with no sender.
	 *
	 * @param message the message
	 */
	public void publish(final Serializable message) {
		mediator.tell(new Publish(name, message), noSender());
	}

	/**
	 * Publish message to this topic.
	 *
	 * @param message the message
	 * @param sender the message sender
	 */
	public void publish(final Serializable message, final ActorRef sender) {
		mediator.tell(new Publish(name, message), sender);
	}
}
