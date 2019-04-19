package com.github.sarxos.abberwoult.cdi.arc;

public class SomeSystem {
	public SomeRef refOf(String name) {
		return new SomeRef(this, name);
	}
}
