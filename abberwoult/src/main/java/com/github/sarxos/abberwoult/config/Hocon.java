package com.github.sarxos.abberwoult.config;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.eclipse.microprofile.config.spi.ConfigSource;

import com.typesafe.config.ConfigValue;


public class Hocon implements ConfigSource {

	private static final int ORDINAL = 900;
	private static final String NAME = Hocon.class.getSimpleName();
	private static final Function<Entry<String, ConfigValue>, String> KEY_MAPPER = entry -> entry.getKey();
	private static final Function<Entry<String, ConfigValue>, String> VAL_MAPPER = entry -> entry.getValue().unwrapped().toString();

	private final Map<String, String> properties = ApplicationConfLoader.load()
		.entrySet()
		.stream()
		.collect(toMap(KEY_MAPPER, VAL_MAPPER));

	@Override
	public int getOrdinal() {
		return ORDINAL;
	}

	@Override
	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public String getValue(final String key) {
		return properties.get(key);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
