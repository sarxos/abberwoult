package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.ReflectionUtils.getClazz;

import io.quarkus.runtime.annotations.Template;


@Template
public class PreStartRegistryTemplate {

	public void register(final String className) {
		PreStartRegistry.register(getClazz(className));
	}
}
