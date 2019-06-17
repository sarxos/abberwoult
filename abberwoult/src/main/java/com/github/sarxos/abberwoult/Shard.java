package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.AskableActorUtils.throwIfThrowable;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.pattern.Patterns;
import akka.util.Timeout;


/**
 * Injectable {@link Askable} instance used to communicate with a sharding. This class is meant to
 * be injectable but it can also be extended and used as a regular POJO.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class Shard implements Askable<ShardRoutableMessage> {

	/**
	 * A shard region {@link ActorRef} used to communicate with sharding.
	 */
	private final ActorRef region;

	/**
	 * The ask {@link Timeout}.
	 */
	private final Duration timeout;

	/**
	 * @param region the shard region
	 * @param timeout the ask timeout
	 */
	Shard(final ActorRef region, final Duration timeout) {
		this.region = region;
		this.timeout = timeout;
	}

	@Override
	public <T> CompletionStage<T> ask(final ShardRoutableMessage message, final Duration timeout) {
		return throwIfThrowable(Patterns.ask(region, message, timeout));
	}

	@Override
	public void tell(final ShardRoutableMessage message, ActorRef sender) {
		region.tell(message, sender);
	}

	@Override
	public Duration getTimeout() {
		return timeout;
	}
}
