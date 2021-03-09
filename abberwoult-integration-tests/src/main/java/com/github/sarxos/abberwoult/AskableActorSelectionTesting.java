package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.NamedActor;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.dsl.Disposers;
import com.github.sarxos.abberwoult.dsl.Utils;


public class AskableActorSelectionTesting {

	@NamedActor("askableselectiontest")
	public static class AskableActorSelectionTestClass extends SimpleActor implements Utils, Disposers {

		public void handleInteger(@Received final Integer i) {
			replyAndDispose(i);
		}

		public void handleThrowable(@Received final Throwable t) {
			replyAndDispose(t);
		}

		private void replyAndDispose(final Object value) {
			reply(value);
			dispose();
		}
	}

}
