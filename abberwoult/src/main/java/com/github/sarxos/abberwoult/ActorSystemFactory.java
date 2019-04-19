package com.github.sarxos.abberwoult;

import java.util.UUID;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import akka.actor.ActorSystem;


@Singleton
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
