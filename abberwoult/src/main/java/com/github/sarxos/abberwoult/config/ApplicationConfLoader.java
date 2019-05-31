package com.github.sarxos.abberwoult.config;

import java.io.File;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;


public class ApplicationConfLoader {

	public static final String APPLICATION_CONF_FILE_NAME = "application.properties";

	private static final ConfigParseOptions OPTS = ConfigParseOptions
		.defaults()
		.setSyntax(ConfigSyntax.CONF);

	public static final Config load() {
		return ConfigFactory.parseFile(new File(APPLICATION_CONF_FILE_NAME), OPTS)
			.withFallback(ConfigFactory.parseResources(APPLICATION_CONF_FILE_NAME, OPTS))
			.resolve();
	}
}
