package com.github.sarxos.abberwoult.cdi.internals;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.annotation.Assisted;


public final class CdiTestInjectServiceByFieldWithAssistedArgs {

	@Inject
	CdiDummyService service;

	final int x;
	final int y;

	@Inject
	CdiTestInjectServiceByFieldWithAssistedArgs(@Assisted int x, @Assisted int y) {
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
