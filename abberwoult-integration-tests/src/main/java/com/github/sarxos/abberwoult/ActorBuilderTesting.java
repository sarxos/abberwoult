package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.NamedActor;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ActorBuilderTesting {

	public static class ReplyIntegerActor extends SimpleActor implements Utils {

		public void handleInteger(@Received Integer i) {
			reply(i);
		}
	}

	public static class ReplyPathAnnonymousActor extends SimpleActor implements Utils {

		public void handleInteger(@Received Integer i) {
			reply(self().path().name());
		}
	}

	@NamedActor("bubu")
	public static class ReplyPathNamedActor extends SimpleActor implements Utils {

		public void handleInteger(@Received Integer i) {
			reply(self().path().name());
		}
	}
}
