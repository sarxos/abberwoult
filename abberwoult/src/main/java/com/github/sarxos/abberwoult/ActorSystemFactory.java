package com.github.sarxos.abberwoult;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.github.sarxos.abberwoult.config.ApplicationConfLoader;

import akka.actor.ActorSystem;


@Singleton
public class ActorSystemFactory {

	@ConfigProperty(name = "akka.actor.system.name", defaultValue = "abberwoult")
	String systemName;

	@Produces
	@Singleton
	public ActorSystem create() {
		return ActorSystem.create(getName(), ApplicationConfLoader.load());
	}

	private String getName() {
		return systemName;
	}
}
