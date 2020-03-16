package com.github.sarxos.abberwoult.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ShardMessageExtractor;
import com.github.sarxos.abberwoult.annotation.ShardEntityIdTesting.ShardEntityIdBooleanMsg;
import com.github.sarxos.abberwoult.annotation.ShardEntityIdTesting.ShardEntityIdByteMsg;
import com.github.sarxos.abberwoult.annotation.ShardEntityIdTesting.ShardEntityIdDoubleMsg;
import com.github.sarxos.abberwoult.annotation.ShardEntityIdTesting.ShardEntityIdFloatMsg;
import com.github.sarxos.abberwoult.annotation.ShardEntityIdTesting.ShardEntityIdIntMsg;
import com.github.sarxos.abberwoult.annotation.ShardEntityIdTesting.ShardEntityIdShortMsg;
import com.github.sarxos.abberwoult.annotation.ShardEntityIdTesting.ShardEntityIdStringMsg;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ShardIdIT {

	@Inject
	ShardMessageExtractor extractor;

	@Test
	public void test_stringShardIdExtraction() {
		assertThat(extractor.shardId(new ShardEntityIdStringMsg("bubu"))).isEqualTo("10");
	}

	@Test
	public void test_byteShardIdExtraction() {
		assertThat(extractor.shardId(new ShardEntityIdByteMsg((byte) 5))).isEqualTo("5");
		assertThat(extractor.shardId(new ShardEntityIdByteMsg((byte) 105))).isEqualTo("5");
	}

	@Test
	public void test_shortShardIdExtraction() {
		assertThat(extractor.shardId(new ShardEntityIdShortMsg((short) 6))).isEqualTo("6");
		assertThat(extractor.shardId(new ShardEntityIdShortMsg((short) 106))).isEqualTo("6");
	}

	@Test
	public void test_intShardIdExtraction() {
		assertThat(extractor.shardId(new ShardEntityIdIntMsg(7))).isEqualTo("7");
		assertThat(extractor.shardId(new ShardEntityIdIntMsg(107))).isEqualTo("7");
	}

	@Test
	public void test_floatShardIdExtraction() {
		assertThat(extractor.shardId(new ShardEntityIdFloatMsg(8.0f))).isEqualTo("40");
		assertThat(extractor.shardId(new ShardEntityIdFloatMsg(108.0f))).isEqualTo("32");
	}

	@Test
	public void test_doubleShardIdExtraction() {
		assertThat(extractor.shardId(new ShardEntityIdDoubleMsg(9.0d))).isEqualTo("48");
		assertThat(extractor.shardId(new ShardEntityIdDoubleMsg(109.0d))).isEqualTo("84");
	}

	@Test
	public void test_booleanShardIdExtraction() {
		assertThat(extractor.shardId(new ShardEntityIdBooleanMsg(true))).isEqualTo("31");
		assertThat(extractor.shardId(new ShardEntityIdBooleanMsg(false))).isEqualTo("37");
	}
}
