package com.github.sarxos.abberwoult.example;

import com.github.sarxos.abberwoult.settings.SettingsProvider;
import org.jvnet.hk2.annotations.Service;

@Service
public class ExampleSettingsProvider implements SettingsProvider {

	@Override
	public <T> T get(String section, String key, T def, Class<T> clazz) {
		return null;
	}
}
