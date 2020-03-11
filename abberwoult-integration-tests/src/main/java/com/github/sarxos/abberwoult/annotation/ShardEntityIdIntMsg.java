package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.ShardRoutableMessage;


@SuppressWarnings("serial")
public final class ShardEntityIdIntMsg implements ShardRoutableMessage {

	@ShardId
	@ShardEntityId
	private final int foo;

	public ShardEntityIdIntMsg(int foo) {
		this.foo = foo;
	}

	public int getFoo() {
		return foo;
	}
}
