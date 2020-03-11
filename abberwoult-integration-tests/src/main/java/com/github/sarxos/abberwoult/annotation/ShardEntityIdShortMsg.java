package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.ShardRoutableMessage;


@SuppressWarnings("serial")
public final class ShardEntityIdShortMsg implements ShardRoutableMessage {

	@ShardId
	@ShardEntityId
	private final short foo;

	public ShardEntityIdShortMsg(short foo) {
		this.foo = foo;
	}

	public short getFoo() {
		return foo;
	}
}
