package com.github.sarxos.abberwoult.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ShardMessageExtractor;
import com.github.sarxos.abberwoult.ShardRoutableMessage;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ShardIdTest {

	@Inject
	ShardMessageExtractor extractor;

	@SuppressWarnings("serial")
	public static final class TestStringMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final String foo;

		public TestStringMsg(String foo) {
			this.foo = foo;
		}

		public String getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class TestByteMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final byte foo;

		public TestByteMsg(byte foo) {
			this.foo = foo;
		}

		public byte getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class TestShortMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final short foo;

		public TestShortMsg(short foo) {
			this.foo = foo;
		}

		public short getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class TestIntMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final int foo;

		public TestIntMsg(int foo) {
			this.foo = foo;
		}

		public int getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class TestFloatMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final float foo;

		public TestFloatMsg(float foo) {
			this.foo = foo;
		}

		public float getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class TestDoubleMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final double foo;

		public TestDoubleMsg(double foo) {
			this.foo = foo;
		}

		public double getFoo() {
			return foo;
		}
	}

	@SuppressWarnings("serial")
	public static final class TestBooleanMsg implements ShardRoutableMessage {

		@ShardId
		@ShardEntityId
		private final boolean foo;

		public TestBooleanMsg(boolean foo) {
			this.foo = foo;
		}

		public boolean isFoo() {
			return foo;
		}
	}

	@Test
	public void test_string() {
		assertThat(extractor.shardId(new TestStringMsg("bubu"))).isEqualTo("10");
		assertThat(extractor.shardId(new TestByteMsg((byte) 5))).isEqualTo("5");
		assertThat(extractor.shardId(new TestByteMsg((byte) 105))).isEqualTo("5");
		assertThat(extractor.shardId(new TestShortMsg((short) 6))).isEqualTo("6");
		assertThat(extractor.shardId(new TestShortMsg((short) 106))).isEqualTo("6");
		assertThat(extractor.shardId(new TestIntMsg(7))).isEqualTo("7");
		assertThat(extractor.shardId(new TestIntMsg(107))).isEqualTo("7");
		assertThat(extractor.shardId(new TestFloatMsg(8.0f))).isEqualTo("40");
		assertThat(extractor.shardId(new TestFloatMsg(108.0f))).isEqualTo("32");
		assertThat(extractor.shardId(new TestDoubleMsg(9.0d))).isEqualTo("48");
		assertThat(extractor.shardId(new TestDoubleMsg(109.0d))).isEqualTo("84");
		assertThat(extractor.shardId(new TestBooleanMsg(true))).isEqualTo("31");
		assertThat(extractor.shardId(new TestBooleanMsg(false))).isEqualTo("37");
	}

}
