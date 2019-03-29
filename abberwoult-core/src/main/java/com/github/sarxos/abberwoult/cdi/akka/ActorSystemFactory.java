package com.github.sarxos.abberwoult.cdi.akka;

import java.util.UUID;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import akka.actor.ActorSystem;


public class ActorSystemFactory {

	@Produces
	@Singleton
	public ActorSystem create() {
		return ActorSystem.create(getName());
	}

	private String getName() {
		return UUID.randomUUID().toString();
	}
}
