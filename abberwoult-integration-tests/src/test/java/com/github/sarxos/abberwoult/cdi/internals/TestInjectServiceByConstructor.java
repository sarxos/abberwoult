package com.github.sarxos.abberwoult.cdi.internals;

import javax.inject.Inject;


public final class TestInjectServiceByConstructor {

	final DummyService service;

	@Inject
	TestInjectServiceByConstructor(DummyService service) {
		this.service = service;
	}

	public DummyService getService() {
		return service;
	}
}
