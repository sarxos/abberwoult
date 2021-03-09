package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.NamedActor;
import com.github.sarxos.abberwoult.annotation.Received;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ActorUniverseTesting {

	public static class ActorUniverseTestActor extends SimpleActor implements Utils {
		public void onInteger(@Received Integer i) {
			reply(i);
		}
	}

	@NamedActor("universefoo")
	public static class ActorUniverseNamedUniverseFooActor extends SimpleActor implements Utils {

		public void onInteger(@Received Integer i) {
			reply(i);
		}

		public void onString(@Received String s) {
			reply(self().path().toString());
		}
	}

	@NamedActor
	public static class ActorUniverseNamedActor extends SimpleActor implements Utils {
		public void onInteger(@Received Integer i) {
			reply(i);
		}
	}
}
