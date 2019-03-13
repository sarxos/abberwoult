package com.github.sarxos.abberwoult.settings;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;


public class SettingsUpdateEvent {

	public class Matcher {

		private String section;
		private Set<String> keys = new HashSet<>(6, 1);
		private boolean anyKey = false;

		public Matcher section(String section) {
			this.section = section;
			return this;
		}

		public Matcher key(String key) {
			keys.add(key);
			return this;
		}

		public Matcher keys(String... key) {
			for (String k : key) {
				key(k);
			}
			return this;
		}

		public Matcher anyKey() {
			anyKey = true;
			return this;
		}

		public Matcher then(Runnable run) {

			final Set<String> updated = diff.get(section);

			if (updated == null) {
				return this;
			}

			boolean keyMatch = keys
				.stream()
				.filter(updated::contains)
				.findFirst()
				.isPresent();

			if (keyMatch || anyKey) {
				run.run();
			}

			return this;
		}
	}

	private final Map<String, Set<String>> diff;

	public SettingsUpdateEvent(Map<String, Set<String>> diff) {
		this.diff = diff;
	}

	public Map<String, Set<String>> getDiff() {
		return diff;
	}

	public Matcher match() {
		return new Matcher();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).build();
	}
}
