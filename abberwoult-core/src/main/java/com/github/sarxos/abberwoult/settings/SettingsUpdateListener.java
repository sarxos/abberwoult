package com.github.sarxos.abberwoult.settings;

/**
 * This listener can be added into the {@link SettingsProvider} to listen for settings updates.
 *
 * @author Bartosz Firyn (sarxos)
 */
@FunctionalInterface
public interface SettingsUpdateListener {

	/**
	 * This method is invoked when settings were modified.
	 *
	 * @param event the {@link SettingsUpdateEvent} which contains updated settings
	 */
	void settingsUpdated(SettingsUpdateEvent event);
}
