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

	public static class MessageClassInherit0 {
	}

	public static class MessageClassInherit1 extends MessageClassInherit0 {
	}

	public static class MessageClassInherit2 extends MessageClassInherit1 {
	}

	public static class MessageClassInherit3 extends MessageClassInherit2 {
	}

	public static class MessageClassInherit4 extends MessageClassInherit3 {
	}

	public static class ReceivesMessageClassInherit extends SimpleActor implements Utils {

		public void onMessageClassInherit0(@Receives MessageClassInherit0 m) {
			reply(0);
		}

		public void onMessageClassInherit1(@Receives MessageClassInherit1 m) {
			reply(1);
		}

		public void onMessageClassInherit2(@Receives MessageClassInherit2 m) {
			reply(2);
		}

		public void onMessageClassInherit3(@Receives MessageClassInherit3 m) {
			reply(3);
		}

		public void onMessageClassInherit4(@Receives MessageClassInherit4 m) {
			reply(4);
		}
	}
}
