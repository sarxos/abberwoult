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
		assertThat(extractor.shardId(new TestStringMsg("bubu"))).isEqualTo("41");
		assertThat(extractor.shardId(new TestIntMsg(5))).isEqualTo("84");
		assertThat(extractor.shardId(new TestBooleanMsg(true))).isEqualTo("69");
	}

}
