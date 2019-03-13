package com.github.sarxos.abberwoult.settings;

import org.jvnet.hk2.annotations.Contract;


/**
 * This is {@link Contract} for every {@link SettingsProvider} implementation in existence.
 *
 * @author Bartosz Firyn (sarxos)
 */
@Contract
public interface SettingsProvider {

	/**
	 * Get settings from a given key under a given section. Read either single or multiple value. If
	 * given value cannot be found then default value is returned.
	 *
	 * @param section the section name
	 * @param key the kay name
	 * @param def the default value
	 * @param clazz the class of value read
	 * @param isMultiValued is setting multi-valued (true) or single-valued (false)
	 * @return Read value
	 */
	<T> T get(String section, String key, T def, Class<T> clazz);

	/**
	 * Add {@link SettingsUpdateListener} to the provider. This listener will be notified when
	 * changes are made in settings.
	 *
	 * @param l the listener instance to be added
	 */
	default void addUpdateListener(SettingsUpdateListener l) {
		// do nothing
	}

	/**
	 * Remove {@link SettingsUpdateListener} from the provider.
	 *
	 * @param l the listener to be removed
	 */
	default void removeUpdateListener(SettingsUpdateListener l) {
		// do nothing
	}
}
