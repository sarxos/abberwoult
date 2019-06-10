package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.AskableActorUtils.throwIfThrowable;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorRef;
import akka.pattern.PatternsCS;
import akka.util.Timeout;


public class Shard implements Askable<ShardRoutableMessage> {

	private final String name;
	private final Timeout timeout;
	private final ActorRef region;

	public Shard(final String name, final Timeout timeout, final ActorRef region) {
		this.name = name;
		this.timeout = timeout;
		this.region = region;
	}

	public String getName() {
		return name;
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
