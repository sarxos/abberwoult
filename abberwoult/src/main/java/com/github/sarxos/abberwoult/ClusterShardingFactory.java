package com.github.sarxos.abberwoult;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;


/**
 * A factory bean which creates {@link ClusterSharding}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ClusterShardingFactory {

	private final ClusterSharding sharding;

	@Inject
	public ClusterShardingFactory(final ActorSystem system) {
		this.sharding = ClusterSharding.get(system);
	}

	@Produces
	public ClusterSharding create() {
		return sharding;
	}
}
