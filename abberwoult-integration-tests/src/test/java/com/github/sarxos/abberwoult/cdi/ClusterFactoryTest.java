package com.github.sarxos.abberwoult.cdi;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import akka.cluster.Cluster;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ClusterFactoryTest {

	@Inject
	Cluster cluster;

	@Test
	public void test_injectCluster() {
		assertThat(cluster).isNotNull();
	}
}
