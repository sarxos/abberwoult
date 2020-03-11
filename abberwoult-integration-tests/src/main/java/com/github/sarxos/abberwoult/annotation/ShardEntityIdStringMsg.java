package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.ShardRoutableMessage;


@SuppressWarnings("serial")
public final class ShardEntityIdStringMsg implements ShardRoutableMessage {

	@ShardId
	@ShardEntityId
	private final String foo;

	public ShardEntityIdStringMsg(final String foo) {
		this.foo = foo;
	}

	public String getFoo() {
		return foo;
	}
}
