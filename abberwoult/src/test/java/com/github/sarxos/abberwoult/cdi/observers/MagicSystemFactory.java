package com.github.sarxos.abberwoult.cdi.observers;

import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;


@Singleton
public class MagicSystemFactory {

	@Produces
	@Singleton
	public MagicSystem create() {
		return new MagicSystem();
	}

	void dipose(@Disposes MagicSystem value) {
		System.out.println("disposed");
	}
}
