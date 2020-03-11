package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.ShardRoutableMessage;


@SuppressWarnings("serial")
public final class ShardEntityIdLongMsg implements ShardRoutableMessage {

	@ShardId
	@ShardEntityId
	private final long foo;

	public ShardEntityIdLongMsg(long foo) {
		this.foo = foo;
	}

	public long getFoo() {
		return foo;
	}
}
