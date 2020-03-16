package com.github.sarxos.abberwoult.annotation;

import com.github.sarxos.abberwoult.ShardRoutableMessage;


public class ShardEntityIdTesting {

	@SuppressWarnings("serial")
	public static final class ShardEntityIdBooleanMsg implements ShardRoutableMessage {

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

	@SuppressWarnings("serial")
	public static final class ShardEntityIdByteMsg implements ShardRoutableMessage {

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

	@SuppressWarnings("serial")
	public static final class ShardEntityIdDoubleMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final double foo;

		public ShardEntityIdDoubleMsg(double foo) {
			this.foo = foo;
		}

		public double getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class ShardEntityIdFloatMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final float foo;

		public ShardEntityIdFloatMsg(float foo) {
			this.foo = foo;
		}

		public float getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class ShardEntityIdIntMsg implements ShardRoutableMessage {

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

	@SuppressWarnings("serial")
	public static final class ShardEntityIdLongMsg implements ShardRoutableMessage {

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

	@SuppressWarnings("serial")
	public static final class ShardEntityIdShortMsg implements ShardRoutableMessage {

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

	@SuppressWarnings("serial")
	public static final class ShardEntityIdStringMsg implements ShardRoutableMessage {

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
}
