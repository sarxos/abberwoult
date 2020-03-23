package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.dsl.Utils;


public class ReceivesTesting {

	public static class ReceivesActor extends SimpleActor implements Utils {

		public void onInteger(@Receives Integer index) {
			reply(11);
		}
	}

	public static class ReceivesActorSubclass extends ReceivesActorSuperclass {

	}

	public static class ReceivesActorSuperclass extends SimpleActor implements Utils {

		public void onIntegerXXX(@Receives Integer i) {
			reply(22);
		}
	}
}
