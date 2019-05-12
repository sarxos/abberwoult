package com.github.sarxos.abberwoult.config;

import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.spi.Converter;

import akka.util.Timeout;


public class TimeoutConverter implements Converter<Timeout> {

	@Override
	public Timeout convert(final String value) {
		final long duration = Long.valueOf(value);
		final TimeUnit unit = TimeUnit.SECONDS;
		return Timeout.apply(duration, unit);
	}
}
