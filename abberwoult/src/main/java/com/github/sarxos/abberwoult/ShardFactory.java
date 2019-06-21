package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getLabel;

import java.time.Duration;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.config.AskTimeout;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;


/**
 * This is factory bean which create {@link Shard} instances which were annotated with
 * {@link Labeled} annotation. A {@link Labeled} annotation acts as a name of shard to be injected.
 * The resultant {@link Shard} is {@link Askable}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ShardFactory {

	/**
	 * The {@link ClusterSharding} from {@link ActorSystem}.
	 */
	private final ClusterSharding sharding;

	/**
	 * Ask timeout.
	 */
	private final Duration timeout;

	@Inject
	public ShardFactory(final ClusterSharding sharding, @AskTimeout Duration timeout) {
		this.sharding = sharding;
		this.timeout = timeout;
	}

	/**
	 * Create labeled {@link Shard} instance with {@link Dependent} scope. This method is meant to
	 * be invoked by the CDI SPI.
	 *
	 * @param injection the {@link InjectionPoint} provided by CDI SPI
	 * @return New {@link Shard} instance
	 */
	@Produces
	@Labeled
	public Shard create(final InjectionPoint injection) {

		final String name = getLabel(injection);
		final ActorRef region = sharding.shardRegion(name);
		final Shard shard = new Shard(region, timeout);

		return shard;
	}
}
