package com.github.sarxos.abberwoult;

import com.github.sarxos.abberwoult.annotation.ShardEntityId;
import com.github.sarxos.abberwoult.annotation.ShardId;


public class ShardTesting {

	@SuppressWarnings("serial")
	public static final class SomeMsg implements ShardRoutableMessage {

		@ShardId
		private final String route;

		@ShardEntityId
		private final String id;

		public SomeMsg(String route, String id) {
			this.route = route;
			this.id = id;
		}

		public String getRoute() {
			return route;
		}

		public String getId() {
			return id;
		}
	}
}
