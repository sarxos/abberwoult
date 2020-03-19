package com.github.sarxos.abberwoult.cdi.internals;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.annotation.Assisted;


public final class CdiTestInjectServiceByConstructorWithAssistedArgs {

	final CdiDummyService service;
	final int x;
	final int y;

	@Inject
	CdiTestInjectServiceByConstructorWithAssistedArgs(CdiDummyService service, @Assisted int x, @Assisted int y) {
		this.service = service;
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
