package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Disposers;
import com.github.sarxos.abberwoult.dsl.Utils;


public class AskableActorSelectionTesting {

	@Named("askableselectiontest")
	public static class AskableActorSelectionTestClass extends SimpleActor implements Utils, Disposers {

		public void handleInteger(@Receives final Integer i) {
			replyAndDispose(i);
		}

		public void handleThrowable(@Receives final Throwable t) {
			replyAndDispose(t);
		}

		private void replyAndDispose(final Object value) {
			reply(value);
			dispose();
		}
	}

}
