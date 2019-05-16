package com.github.sarxos.abberwoult;

import akka.actor.ActorRef;


public interface Tellable {

	void tell(final Object message);

	void tell(final Object message, final ActorRef sender);
}
