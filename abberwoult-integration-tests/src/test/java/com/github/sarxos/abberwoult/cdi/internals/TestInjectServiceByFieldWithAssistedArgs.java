package com.github.sarxos.abberwoult.cdi.internals;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.annotation.Assisted;


public final class TestInjectServiceByFieldWithAssistedArgs {

	@Inject
	DummyService service;

	final int x;
	final int y;

	@Inject
	TestInjectServiceByFieldWithAssistedArgs(@Assisted int x, @Assisted int y) {
		this.x = x;
		this.y = y;
	}

	public DummyService getService() {
		return service;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}