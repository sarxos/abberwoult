package com.github.sarxos.abberwoult.cdi.arc;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;


@Singleton
public class SomeSystemFactory {

	@Produces
	@Singleton
	public SomeSystem create() {
		return new SomeSystem();
	}
}
