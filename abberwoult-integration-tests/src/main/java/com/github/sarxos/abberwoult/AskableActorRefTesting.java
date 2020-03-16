package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Disposers;
import com.github.sarxos.abberwoult.dsl.Utils;


public class AskableActorRefTesting {

	public static class AskableActorRefTestActor extends SimpleActor implements Utils, Disposers {

		public void onInteger(@Receives final Integer i) {
			replyAndDispose(i);
		}

		public void onThrowable(@Receives final Throwable t) {
			replyAndDispose(t);
		}

		private void replyAndDispose(final Object value) {
			reply(value);
			dispose();
		}
	}
}
