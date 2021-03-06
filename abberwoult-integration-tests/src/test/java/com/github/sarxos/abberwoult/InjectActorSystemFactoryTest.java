package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import akka.actor.ActorSystem;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class InjectActorSystemFactoryTest {

	@Inject
	ActorSystem system;

	@Test
	void test_injectActorRefByClass() {
		assertThat(system).isNotNull();
	}
}
