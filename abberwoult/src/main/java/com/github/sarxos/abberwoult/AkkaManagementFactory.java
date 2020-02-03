package com.github.sarxos.abberwoult;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.management.javadsl.AkkaManagement;


@Singleton
public class AkkaManagementFactory {

	@Inject
	ActorSystem system;

	@Produces
	@Singleton
	public AkkaManagement create() {
		return AkkaManagement.get(system);
	}
}
