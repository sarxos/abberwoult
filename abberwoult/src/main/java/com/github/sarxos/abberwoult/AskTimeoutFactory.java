package com.github.sarxos.abberwoult;

import java.time.Duration;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.sarxos.abberwoult.config.AskTimeout;

import akka.util.Timeout;
import scala.concurrent.duration.FiniteDuration;


/**
 * Factory for {@link AskTimeout} qualified {@link Timeout} and {@link Duration} fields.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class AskTimeoutFactory {

	private static final String P_VALUE = "10";
	private static final String P_NAME = "akka.default-timeout";

	private final Timeout timeout;
	private final Duration duration;
	private final FiniteDuration finiteDuration;

	@Inject
	public AskTimeoutFactory(@ConfigProperty(name = P_NAME, defaultValue = P_VALUE) final Timeout timeout) {
		this.timeout = timeout;
		this.duration = durationOf(timeout);
		this.finiteDuration = timeout.duration();
	}

	@Produces
	@AskTimeout
	public Timeout getTimeout() {
		return timeout;
	}

	@Produces
	@AskTimeout
	public Duration getDuration() {
		return duration;
	}

	@Produces
	@AskTimeout
	public FiniteDuration getFiniteDuration() {
		return finiteDuration;
	}

	public static Duration durationOf(final Timeout timeout) {
		return Duration.ofNanos(timeout.duration().toNanos());
	}
}
