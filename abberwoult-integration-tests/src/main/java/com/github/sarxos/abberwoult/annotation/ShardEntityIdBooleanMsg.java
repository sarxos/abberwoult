package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.ShardRoutableMessage;


@SuppressWarnings("serial")
public final class ShardEntityIdBooleanMsg implements ShardRoutableMessage {

	@ShardId
	@ShardEntityId
	private final boolean foo;

	public ShardEntityIdBooleanMsg(boolean foo) {
		this.foo = foo;
	}

	public boolean isFoo() {
		return foo;
	}
}
