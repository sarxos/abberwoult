package com.github.sarxos.abberwoult.settings;

import static java.util.Objects.requireNonNull;

import io.vavr.control.Option;
import java.io.File;
import java.util.EnumSet;


/**
 * Abstract settings class. It wraps {@link SettingsProvider} in a sleek API and gives possibility
 * to inject it in injectable context.
 *
 * @author Bartosz Firyn (sarxos)
 */
public abstract class Settings {

	/**
	 * Atomic reference to associated provider object.
	 */
	private final SettingsProvider provider;

	/**
	 * Create settings object with a given {@link SettingsProvider}.
	 *
	 * @param provider the settings provider
	 */
	public Settings(final SettingsProvider provider) {
		this.provider = requireNonNull(provider, "Settings provider must not be null!");
	}

	/**
	 * Return settings value from given section and given key or null if key has not been found.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @return {@link String} value or null if key has not been found
	 */
	protected String get(String section, String key) {
		return get(section, key, (String) null);
	}

	/**
	 * Return settings value from given section and given key.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param def the default value
	 * @return {@link String} value or default if key has not been found
	 */
	protected String get(String section, String key, String def) {
		return get(section, key, def, String.class);
	}

	protected <T> T get(String section, String key, Class<T> clazz) {
		return get(section, key, null, clazz);
	}

	/**
	 * Get value from given section and given key and return default value if key not found. O(In
	 * case of multiple key it will return collection.
	 *
	 * @param <T> the generic type to be returned
	 * @param section the section name
	 * @param key the key name
	 * @param def the default value
	 * @param clazz the value type
	 * @return Value from key (or keys)
	 */
	protected <T> T get(String section, String key, T def, Class<T> clazz) {
		return Option
			.of(provider.get(section, key, def, clazz))
			.getOrElse(def);
	}

	/**
	 * @return {@link SettingsProvider} associated with this settings object
	 */
	protected SettingsProvider provider() {
		return provider;
	}

	/**
	 * Read integer value from given section and given key. If section or key has not been found it
	 * will return -1 value.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @return Integer value or -1 if not found
	 */
	protected int getInt(String section, String key) {
		return get(section, key, -1, Integer.class);
	}

	/**
	 * Read integer value from given section and given key. If section or key has not been found it
	 * will return default value.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param def the default value
	 * @return Integer value
	 */
	protected int getInt(String section, String key, int def) {
		return get(section, key, def, Integer.class);
	}

	/**
	 * Read double value from given section and given key. If section or key has not been found it
	 * will return default value.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param def the default value
	 * @return Double value
	 */
	protected double getDouble(String section, String key, double def) {
		return get(section, key, def, Double.class);
	}

	/**
	 * Read long value from given section and given key. If section or key has not been found it
	 * will return -1 value.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @return Long value
	 */
	protected long getLong(String section, String key) {
		return get(section, key, -1L, Long.class);
	}

	/**
	 * Read long value from given section and given key. If section or key has not been found it
	 * will return default value.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param def the default value
	 * @return Long value
	 */
	protected long getLong(String section, String key, long def) {
		return get(section, key, def, Long.class);
	}

	/**
	 * Read boolean variable from given section and given key.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @return Boolean value associated with given key
	 */
	protected boolean getBoolean(String section, String key) {
		return get(section, key, Boolean.class);
	}

	/**
	 * Read boolean variable from given section and given key. If key is not found it will return
	 * default value.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param def the default value
	 * @return Boolean value associated with given key
	 */
	protected boolean getBoolean(String section, String key, boolean def) {
		return get(section, key, def, Boolean.class);
	}

	/**
	 * Read given key from given section and change it to file path. Then return file that points to
	 * this abstract path.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @return {@link File} object that points to abstract filesystem path from the key
	 */
	protected File getFile(String section, String key) {
		return new File(get(section, key));
	}

	/**
	 * Read given key from given section and change it to file path. Then return file that points to
	 * this abstract path.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param defPath the default path if not specifies in settings provider
	 * @return {@link File} object that points to abstract filesystem path from the key
	 */
	protected File getFile(String section, String key, String defPath) {
		return new File(get(section, key, defPath));
	}

	/**
	 * Read given key from given section and converts it to enum value of the class given in the
	 * argument. It return null if key does not contain any value or when value is not a valid enum
	 * constant.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param clazz the enum class
	 * @return Constant from enum class
	 */
	protected <E extends Enum<E>> E getEnum(String section, String key, Class<E> clazz) {
		String constant = get(section, key);
		if (constant != null) {
			for (E element : EnumSet.allOf(clazz)) {
				if (element.name().equalsIgnoreCase(constant)) {
					return element;
				}
			}
		}
		return null;
	}

	/**
	 * Read given key from given section and converts it to enum value of the class given in the
	 * argument. It return default value given in the argument if key does not contain any value or
	 * when value is not a valid enum constant.
	 *
	 * @param section the section name
	 * @param key the key name
	 * @param clazz the enum class
	 * @param def the default value
	 * @return Value from enum class
	 */
	protected <E extends Enum<E>> E getEnum(String section, String key, Class<E> clazz, E def) {
		E value = getEnum(section, key, clazz);
		if (value == null) {
			return def;
		} else {
			return value;
		}
	}
}
