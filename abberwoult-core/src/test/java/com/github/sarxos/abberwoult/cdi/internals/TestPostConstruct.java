package com.github.sarxos.abberwoult.cdi.internals;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;


public class TestPostConstruct {

	private final AtomicBoolean invoked;

	public TestPostConstruct(final AtomicBoolean invoked) {
		this.invoked = invoked;
	}

	@PostConstruct
	void invokeAfterConstruction() {
		invoked.set(true);
	}

	public boolean wasInvoked() {
		return invoked.get();
	}
}
