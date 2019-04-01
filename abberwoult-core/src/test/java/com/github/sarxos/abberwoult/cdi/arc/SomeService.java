package com.github.sarxos.abberwoult.cdi.arc;

import java.util.UUID;

import javax.inject.Singleton;


@Singleton
public class SomeService {
	public String getName() {
		return UUID.randomUUID().toString();
	}
}
