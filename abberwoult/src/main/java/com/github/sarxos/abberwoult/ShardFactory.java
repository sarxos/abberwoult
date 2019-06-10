package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getLabel;
import static com.github.sarxos.abberwoult.util.ActorUtils.DEFAULT_TIMEOUT_PROP;
import static com.github.sarxos.abberwoult.util.ActorUtils.DEFAULT_TIMEOUT_SECONDS;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.sarxos.abberwoult.annotation.Labeled;

import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import akka.util.Timeout;


@Singleton
public class ShardFactory {

	private final ClusterSharding sharding;

	@ConfigProperty(name = DEFAULT_TIMEOUT_PROP, defaultValue = DEFAULT_TIMEOUT_SECONDS)
	Timeout timeout;

	@Inject
	public ShardFactory(final ClusterSharding sharding) {
		this.sharding = sharding;
	}

	@Produces
	@Labeled
	public Shard create(final InjectionPoint injection) {

		final String name = getLabel(injection);
		final ActorRef region = sharding.shardRegion(name);
		final Shard shard = new Shard(name, timeout, region);

		return shard;
	}
}
