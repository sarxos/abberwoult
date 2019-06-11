package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.AskableActorUtils.throwIfThrowable;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;


public class Shard implements Askable<ShardRoutableMessage> {

	private final ActorRef region;
	private final Timeout timeout;

	public Shard(final ActorRef region, final Timeout timeout) {
		this.region = region;
		this.timeout = timeout;
	}

	@Override
	public <T> CompletionStage<T> ask(final ShardRoutableMessage message) {
		return throwIfThrowable(PatternsCS.ask(region, message, timeout));
	}

	@Override
	public <T> CompletionStage<T> ask(final ShardRoutableMessage message, Timeout timeout) {
		return throwIfThrowable(PatternsCS.ask(region, message, timeout));
	}

	@Override
	public <T> CompletionStage<T> ask(final ShardRoutableMessage message, Duration timeout) {
		return throwIfThrowable(PatternsCS.ask(region, message, timeout));
	}

	@Override
	public void tell(final ShardRoutableMessage message, ActorRef sender) {
		region.tell(message, sender);
	}
}
