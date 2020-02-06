package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getLabel;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.annotation.Named;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSub;


/**
 * The factory to create {@link Topic} instances.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class TopicFactory {

	private final DistributedPubSub pubsub;
	private final ActorRef mediator;

	@Inject
	public TopicFactory(final ActorSystem system) {
		this.pubsub = DistributedPubSub.get(system);
		this.mediator = pubsub.mediator();
	}

	@Produces
	@Named
	public Topic create(final InjectionPoint injection) {

		final String name = getLabel(injection);
		final Topic topic = new Topic(name, mediator);

		return topic;
	}
}
