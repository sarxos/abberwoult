package com.github.sarxos.abberwoult;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import akka.actor.ActorSystem;
import akka.event.EventStream;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;


@Singleton
public class QuarkusLifecycleListener {

	private final EventStream stream;

	@Inject
	public QuarkusLifecycleListener(final ActorSystem system) {
		this.stream = system.getEventStream();
	}

	void onStart(@Observes StartupEvent ev) {
		stream.publish(ev);
	}

	void onStop(@Observes ShutdownEvent ev) {
		stream.publish(ev);
	}
}
