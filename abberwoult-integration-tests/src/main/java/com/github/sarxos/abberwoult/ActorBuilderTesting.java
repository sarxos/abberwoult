package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ActorBuilderTesting {

	public static class ReplyIntegerActor extends SimpleActor implements Utils {

		public void handleInteger(@Receives Integer i) {
			reply(i);
		}
	}

	public static class ReplyPathAnnonymousActor extends SimpleActor implements Utils {

		public void handleInteger(@Receives Integer i) {
			reply(self().path().name());
		}
	}

	@Named("bubu")
	public static class ReplyPathNamedActor extends SimpleActor implements Utils {

		public void handleInteger(@Receives Integer i) {
			reply(self().path().name());
		}
	}
}
