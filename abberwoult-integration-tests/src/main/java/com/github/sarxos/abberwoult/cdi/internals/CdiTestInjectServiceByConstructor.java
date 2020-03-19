package com.github.sarxos.abberwoult.cdi.internals;

import javax.inject.Inject;


public final class CdiTestInjectServiceByConstructor {

	final CdiDummyService service;

	@Inject
	CdiTestInjectServiceByConstructor(CdiDummyService service) {
		this.service = service;
	}

	public CdiDummyService getService() {
		return service;
	}
}
