package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.ShardRoutableMessage;


@SuppressWarnings("serial")
public final class ShardEntityIdByteMsg implements ShardRoutableMessage {

	@ShardId
	@ShardEntityId
	private final byte foo;

	public ShardEntityIdByteMsg(byte foo) {
		this.foo = foo;
	}

	public byte getFoo() {
		return foo;
	}
}
