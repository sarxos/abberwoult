package com.github.sarxos.abberwoult;

import akka.actor.Actor;
import akka.japi.pf.FI.UnitApply;


public interface ReceiveInvoker extends UnitApply<Object> {

	public static class UnhandledReceiveInvoker implements ReceiveInvoker {

		private final Actor actor;

		public UnhandledReceiveInvoker(Actor actor) {
			this.actor = actor;
		}

		@Override
		public void apply(Object message) throws Exception {
			actor.unhandled(message);
		}
	}
}
