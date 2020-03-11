package com.github.sarxos.abberwoult.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ShardMessageExtractor;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ShardEntityIdIT {

	@Inject
	ShardMessageExtractor extractor;

	@Test
	public void test_stringShardEntityId() {
		assertThat(extractor.entityId(new ShardEntityIdStringMsg("bubu"))).isEqualTo("bubu");
	}

	@Test
	public void test_intShardEntityId() {
		assertThat(extractor.entityId(new ShardEntityIdIntMsg(5))).isEqualTo("5");
	}

	@Test
	public void test_booleanShardEntityId() {
		assertThat(extractor.entityId(new ShardEntityIdBooleanMsg(true))).isEqualTo("true");
	}

	@Test
	public void test_longShardEntityId() {
		assertThat(extractor.entityId(new ShardEntityIdLongMsg(6L))).isEqualTo(6L);
	}

	@Test
	public void test_shortShardEntityId() {
		assertThat(extractor.entityId(new ShardEntityIdShortMsg((short) 7))).isEqualTo((short) 7);
	}

	@Test
	public void test_byteShardEntityId() {
		assertThat(extractor.entityId(new ShardEntityIdByteMsg((byte) 8))).isEqualTo((byte) 8);
	}
}
