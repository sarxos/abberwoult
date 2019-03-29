package com.github.sarxos.abberwoult.cdi.internals;

import javax.inject.Inject;


public final class TestInjectServiceByFieldWithArgs {

	@Inject
	DummyService service;

	final int x;
	final int y;

	TestInjectServiceByFieldWithArgs(int x, int y) {
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
