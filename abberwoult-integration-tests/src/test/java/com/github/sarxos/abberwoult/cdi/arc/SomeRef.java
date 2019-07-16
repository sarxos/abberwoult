package com.github.sarxos.abberwoult.cdi.arc;

public class SomeRef {

	private final SomeSystem system;
	private final String name;

	public SomeRef(SomeSystem system, String name) {
		this.system = system;
		this.name = name;
	}

	public SomeSystem getSystem() {
		return system;
	}

	public String getName() {
		return name;
	}
}
