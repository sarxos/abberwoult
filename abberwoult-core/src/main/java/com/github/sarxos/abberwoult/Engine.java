package com.github.sarxos.abberwoult;

import akka.actor.ActorSystem;
import javax.inject.Inject;
import org.jvnet.hk2.annotations.Service;

@Service
public class Engine {

	private final ActorSystem system;

	@Inject
	public Engine(final ActorSystem system) {
		this.system = system;
	}

	public ActorSystem getActorSystem() {
		return system;
	}
}
