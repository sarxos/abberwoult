package com.github.sarxos.abberwoult;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;


/**
 * A factory bean which creates {@link Cluster}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ClusterFactory {

	private final Cluster cluster;

	@Inject
	public ClusterFactory(final ActorSystem system) {
		this.cluster = Cluster.get(system);
	}

	@Produces
	public Cluster create() {
		return cluster;
	}
}
