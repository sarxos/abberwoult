package com.github.sarxos.abberwoult.log;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.MDC;

import akka.actor.AbstractActor;
import akka.event.DummyClassForStringSources;
import akka.event.Logging;
import akka.event.Logging.InitializeLogger;


/**
 * This actor receives {@link Logging.LogEvent}s from system bus and logs them with JBoss logger.
 * Please note that this is pure Akka actor (not the actor derived by Abberwoult).
 *
 * @author Bartosz Firyn (sarxos)
 */
class LoggingBridgeActor extends AbstractActor {

	private final Map<String, Logger> loggers = new HashMap<>();

	@Override
	public Receive createReceive() {
		return receiveBuilder()
			.match(InitializeLogger.class, msg -> sender().tell(Logging.loggerInitialized(), self()))
			.match(Logging.Error.class, this::onError)
			.match(Logging.LogEvent.class, this::onLogEvent)
			.build();
	}

	private void onError(final Logging.Error event) {

		final String source = getSource(event);
		final Logger logger = getLogger(source);

		// l = 1 => error
		// l = 2 => warning
		// l = 3 => info
		// l = 4 => debug

		final int l = event.level();
		final Level level = Level.values()[l];
		final Throwable cause = event.cause();

		log(logger, level, event, cause);
	}

	private void onLogEvent(final Logging.LogEvent event) {

		final String source = getSource(event);
		final Logger logger = getLogger(source);

		// l = 1 => error
		// l = 2 => warning
		// l = 3 => info
		// l = 4 => debug

		final int l = event.level();
		final Level level = Level.values()[l];

		log(logger, level, event, null);
	}

	private String getSource(final Logging.LogEvent event) {
		final Class<?> clazz = event.logClass();
		if (clazz == DummyClassForStringSources.class) {
			return event.logSource();
		} else {
			return clazz.getName();
		}
	}

	private Logger getLogger(final String src) {
		return loggers.computeIfAbsent(src, $ -> Logger.getLogger(src));
	}

	/**
	 * Logs {@link Logging.LogEvent}.
	 *
	 * @param logger the {@link Logger} to be used to log
	 * @param level the logging {@link Level}
	 * @param event the {@link Logging.LogEvent} to be logged
	 */
	private void log(final Logger logger, final Level level, final Logging.LogEvent event, final Throwable cause) {

		// do nothing if given logging level is not enabled on a logger

		if (!logger.isEnabled(level)) {
			return;
		}

		final String threadName = event.thread().getName();
		final Object message = event.message();
		final Set<Entry<String, Object>> mdcs = event.getMDC().entrySet();

		final Thread thread = Thread.currentThread();
		final String oldThreadName = thread.getName();
		final String newThreadName = threadName;

		mdcs.forEach(mdc -> MDC.put(mdc.getKey(), mdc.getValue()));
		thread.setName(newThreadName);

		try {
			logger.logf(level, cause, message.toString());
		} finally {
			mdcs.forEach(mdc -> MDC.remove(mdc.getKey()));
			thread.setName(oldThreadName);
		}
	}
}
