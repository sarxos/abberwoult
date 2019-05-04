package com.github.sarxos.abberwoult;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import akka.actor.ActorSystem;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;


@ApplicationScoped
public class AbberwoultLifecycleListener {

	@Inject
	ActorSystem system;

	void onStart(@Observes final StartupEvent ev) {
		system
			.getEventStream()
			.publish(ev);
	}

	void onStop(@Observes final ShutdownEvent ev) {
		system
			.getEventStream()
			.publish(ev);
	}
}
