package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getName;

import java.time.Duration;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.config.AskTimeout;

import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;


/**
 * This is factory bean which create {@link Sharding} instances which were annotated with {@link Named}
 * annotation. A {@link Named} annotation acts as a name of shard to be injected. The resultant
 * {@link Sharding} is {@link Askable}.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ShardingFactory {

	/**
	 * The {@link ClusterSharding} from {@link ActorSystem}.
	 */
	private final ClusterSharding sharding;

	/**
	 * Ask timeout.
	 */
	private final Duration timeout;

	@Inject
	public ShardingFactory(final ClusterSharding sharding, @AskTimeout Duration timeout) {
		this.sharding = sharding;
		this.timeout = timeout;
	}

	/**
	 * Create labeled {@link Sharding} instance with {@link Dependent} scope. This method is meant to
	 * be invoked by the CDI SPI.
	 *
	 * @param injection the {@link InjectionPoint} provided by CDI SPI
	 * @return New {@link Sharding} instance
	 */
	@Produces
	@Named
	public Sharding create(final InjectionPoint injection) {
		return new Sharding(sharding.shardRegion(getName(injection)), timeout);
	}
}
