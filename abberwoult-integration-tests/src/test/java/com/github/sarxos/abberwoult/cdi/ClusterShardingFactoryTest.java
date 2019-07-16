package com.github.sarxos.abberwoult.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import akka.cluster.sharding.ClusterSharding;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ClusterShardingFactoryTest {

	@Inject
	ClusterSharding sharding1;

	@Inject
	ClusterSharding sharding2;

	@Test
	public void test_injectClusterSharding() {
		assertThat(sharding1).isNotNull();
		assertThat(sharding1).isSameAs(sharding2);
	}
}
