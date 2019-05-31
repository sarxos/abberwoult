package com.github.sarxos.abberwoult.cdi.arc;

public class SomeSystem {

	public static volatile int instances = 0;

	public SomeSystem() {
		// System.out.println("Create some system " + (++instances));
	}

	public SomeRef refOf(String name) {
		return new SomeRef(this, name);
	}
}
