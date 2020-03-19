package com.github.sarxos.abberwoult.cdi.internals;

import javax.inject.Inject;


public final class CdiTestInjectServiceByFieldWithArgs {

	@Inject
	CdiDummyService service;

	final int x;
	final int y;

	CdiTestInjectServiceByFieldWithArgs(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public CdiDummyService getService() {
		return service;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
}
