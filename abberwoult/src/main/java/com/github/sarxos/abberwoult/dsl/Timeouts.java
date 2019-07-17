package com.github.sarxos.abberwoult.dsl;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;

import akka.actor.ActorContext;
import akka.actor.ReceiveTimeout;
import akka.util.Timeout;
import scala.concurrent.duration.FiniteDuration;


/**
 * This interface should be implemented by actor class whenever class should be able to deal with a
 * receive timeouts.
 *
 * @author Bartosz Firyn (sarxos)
 */
public interface Timeouts extends Disposers, ActorInternal {

	/**
	 * Value to be returned from overriden {@link #setReceiveTimeout(int)} method to completely
	 * disable receive timeout in actor.
	 */
	public static final int NO_RECEIVE_TIMEOUT = 0;

	/**
	 * Set receive timeout in seconds. A receive timeout defines the inactivity timeout after which
	 * the sending of a {@link ReceiveTimeout} message is triggered. When specified, the actor must
	 * be able to receive {@link ReceiveTimeout} message.
	 *
	 * @param seconds seconds of inactivity which triggers {@link ReceiveTimeout} to be send
	 */
	default void setReceiveTimeoutSeconds(final int seconds) {
		setReceiveTimeout(seconds, TimeUnit.SECONDS);
	}

	/**
	 * Set receive timeout. A receive timeout defines the inactivity timeout after which the sending
	 * of a {@link ReceiveTimeout} message is triggered. When specified, the actor must be able to
	 * receive {@link ReceiveTimeout} message.
	 *
	 * @param timeout the inactivity timeout which triggers {@link ReceiveTimeout} to be send
	 */
	default void setReceiveTimeout(final Timeout timeout) {

		final FiniteDuration duration = timeout.duration();
		final long length = duration.length();
		final TimeUnit unit = duration.unit();

		setReceiveTimeout(length, unit);
	}

	/**
	 * Set receive timeout. A receive timeout defines the inactivity timeout after which the sending
	 * of a {@link ReceiveTimeout} message is triggered. When specified, the actor must be able to
	 * receive {@link ReceiveTimeout} message.
	 *
	 * @param timeout the inactivity timeout which triggers {@link ReceiveTimeout} to be send
	 */
	default void setReceiveTimeout(final Duration timeout) {
		setReceiveTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
	}

	/**
	 * Set receive timeout. A receive timeout defines the inactivity timeout after which the sending
	 * of a {@link ReceiveTimeout} message is triggered. When specified, the actor must be able to
	 * receive {@link ReceiveTimeout} message. To disable timeout completely one should use
	 * {@link #NO_RECEIVE_TIMEOUT} as a timeout length or set timeout length to zero or negative
	 * value which is treated the same as a {@link #NO_RECEIVE_TIMEOUT} value.
	 *
	 * @param length a timeout length (disable timeout by using zero or negative value)
	 * @param unit a timeout {@link TimeUnit}
	 */
	default void setReceiveTimeout(final long length, final TimeUnit unit) {

		final ActorContext context = getContext();
		final boolean receiveTimeoutDisabled = length == NO_RECEIVE_TIMEOUT || length < 0;

		if (receiveTimeoutDisabled) {
			context.setReceiveTimeout(FiniteDuration.Undefined());
		} else {
			context.setReceiveTimeout(FiniteDuration.create(length, unit));
		}
	}

	/**
	 * {@link PreStart} binding which sets up actor entity receive timeout. The
	 * {@link #getReceiveTimeout()} method should be override in order to modify default receive
	 * timeout for this shard entity actor.
	 */
	@PreStart
	default void setupReceiveTimeout() {
		setReceiveTimeout(getReceiveTimeout(), TimeUnit.SECONDS);
	}

	/**
	 * Get actor receive timeout in seconds. A receive timeout defines the inactivity timeout after
	 * which the sending of a {@link ReceiveTimeout} message is triggered. When specified, the actor
	 * should be able to receive {@link ReceiveTimeout} message.
	 *
	 * @return Receive timeout in seconds
	 */
	int getReceiveTimeout();

	/**
	 * A default receiver method for a {@link ReceiveTimeout} message which is triggered after a
	 * configured inactivity period. When this message is received it means that this specific actor
	 * did not received any message for a configured amount of time. this amount is known as a
	 * receive timeout and can be configured by
	 *
	 * @param msg a {@link ReceiveTimeout} message
	 */
	default void onReceiveTimeout(@Receives final ReceiveTimeout timeout) {
		dispose();
	}
}
