package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getQualifier;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.Topic;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.exception.BeanInjectionException;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.pubsub.DistributedPubSub;


@Singleton
public class TopicFactory {

	private final ActorRef mediator;

	@Inject
	public TopicFactory(final ActorSystem system) {
		this.mediator = DistributedPubSub.get(system).mediator();
	}

	@Produces
	@Labeled
	public Topic create(final InjectionPoint injection) {

		if (injection == null) {
			throw new NoDistributedTopicNameException(injection);
		}

		final String name = getTopicName(injection);
		final Topic topic = new Topic(name, mediator);

		return topic;
	}

	/**
	 * Get topic name from {@link Labeled} annotation.
	 *
	 * @param injection the {@link InjectionPoint}
	 * @return Topic name
	 */
	protected String getTopicName(final InjectionPoint injection) {
		return getQualifier(injection, Labeled.class)
			.map(Labeled::value)
			.getOrElseThrow(() -> new NoDistributedTopicNameException(injection));
	}

	@SuppressWarnings("serial")
	public static class NoDistributedTopicNameException extends BeanInjectionException {
		public NoDistributedTopicNameException(final InjectionPoint injection) {
			super("No " + Labeled.class + " annotation was provided for topic " + injection.getMember());
		}
	}

}
