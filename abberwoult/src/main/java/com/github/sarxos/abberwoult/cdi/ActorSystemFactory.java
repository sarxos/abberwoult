package com.github.sarxos.abberwoult.cdi;

import java.util.UUID;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.config.ApplicationConfLoader;

import akka.actor.ActorSystem;


@Singleton
public class ActorSystemFactory {

	@Produces
	@Singleton
	public ActorSystem create() {
		return ActorSystem.create(getName(), ApplicationConfLoader.load());
	}

	private String getName() {
		return UUID.randomUUID().toString();
	}
}
