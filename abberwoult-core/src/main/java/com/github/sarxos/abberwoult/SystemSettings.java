package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.settings.Settings;
import com.github.sarxos.abberwoult.settings.SettingsProvider;
import javax.inject.Inject;


public class SystemSettings extends Settings {

	@Inject
	public SystemSettings(final SettingsProvider provider) {
		super(provider);
	}

	public String getName() {
		return get("system", "actor.system.name", "default");
	}
}
