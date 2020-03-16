package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ActorUniverseTesting {

	public static class ActorUniverseTestActor extends SimpleActor implements Utils {
		public void onInteger(@Receives Integer i) {
			reply(i);
		}
	}

	@Named("foo")
	public static class ActorUniverseNamedFooActor extends SimpleActor implements Utils {

		public void onInteger(@Receives Integer i) {
			reply(i);
		}

		public void onString(@Receives String s) {
			reply(self().path().toString());
		}
	}

	@Named
	public static class ActorUniverseNamedActor extends SimpleActor implements Utils {
		public void onInteger(@Receives Integer i) {
			reply(i);
		}
	}
}
