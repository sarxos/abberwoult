package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.dsl.Disposers;
import com.github.sarxos.abberwoult.dsl.Utils;


public class AskableActorRefTesting {

	public static class AskableActorRefTestActor extends SimpleActor implements Utils, Disposers {

		public void onInteger(@Received final Integer i) {
			replyAndDispose(i);
		}

		public void onThrowable(@Received final Throwable t) {
			replyAndDispose(t);
		}

		private void replyAndDispose(final Object value) {
			reply(value);
			dispose();
		}
	}
}
