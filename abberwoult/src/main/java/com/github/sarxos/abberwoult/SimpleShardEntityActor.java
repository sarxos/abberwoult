package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.trait.Timeouts;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.sharding.ShardRegion.Passivate;


/**
 * This is a father of all shard entity actors, that is the actors which resides inside the sharding.
 * It's worth to note that all sharded actors has a default receive timeout of 300 seconds enabled.
 * 
 * @see Timeouts 
 *
 * @author Bartosz Firyn (sarxos)
 */
public abstract class SimpleShardEntityActor extends SimpleActor implements Timeouts {

	/**
	 * A message used to gracefully dispose shard entity actor and remove it from a shard.
	 */
	private static final Object PASSIVATE = new Passivate(PoisonPill.getInstance());

	/**
	 * A default receive timeout in seconds used by a shard entity actor. The
	 * {@link #getReceiveTimeout()} method should be override in order to modify default receive
	 * timeout for a specific class of shard entity actors.
	 */
	private static final int RECEIVE_TIMEOUT = 300; // seconds

	/**
	 * Gracefully disposes this actor by sending {@link Passivate} message to parent shard. Disposal
	 * is done with {@link Passivate} instead of a clean {@link PoisonPill} because if a message is
	 * already enqueued to the entity when it stops itself the enqueued message in the mailbox will
	 * be dropped. To support graceful passivation without losing such messages the entity actor can
	 * send {@link Passivate} to its parent shard which is done by this method.
	 */
	@Override
	public void dispose() {

		final ActorContext context = getContext();
		final ActorRef parent = context.getParent();
		final ActorRef self = getSelf();

		parent.tell(PASSIVATE, self);
	}

	/**
	 * Get entity actor receive timeout in seconds. A receive timeout defines the inactivity timeout
	 * after which the sending of a {@link ReceiveTimeout} message is triggered. When specified, the
	 * actor should be able to receive {@link ReceiveTimeout} message.
	 *
	 * @return Receive timeout in seconds
	 */
	@Override
	public int getReceiveTimeout() {
		return RECEIVE_TIMEOUT;
	}
}
