package com.github.sarxos.abberwoult.config;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.microprofile.config.ConfigProvider;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;


public class ApplicationConfLoader {

	public static final String APPLICATION_PROP_FILE_NAME = "application.properties";

	private static final ConfigParseOptions OPTS = ConfigParseOptions
		.defaults()
		.setSyntax(ConfigSyntax.PROPERTIES);

	public static final Config load() {

		final org.eclipse.microprofile.config.Config config = ConfigProvider.getConfig();
		final Iterable<String> propertyNames = config.getPropertyNames();
		final Properties properties = new Properties();

		for (String propertyName : propertyNames) {
			final Optional<String> val = config.getOptionalValue(propertyName, String.class);
			if (val.isPresent()) {
				properties.setProperty(propertyName, val.get());
			}
		}

		return ConfigFactory.empty()
			.withFallback(ConfigFactory.parseFile(new File(APPLICATION_PROP_FILE_NAME), OPTS))
			.withFallback(ConfigFactory.parseResources(APPLICATION_PROP_FILE_NAME, OPTS))
			.withFallback(ConfigFactory.parseProperties(properties, OPTS))
			.resolve();
	}
}
