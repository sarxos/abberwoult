package com.github.sarxos.abberwoult;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.ActorOf;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ActorRefFactoryIT {

	@Inject
	@ActorOf(EmptySimpleActor.class)
	ActorRef refS;

	@Inject
	@ActorOf(EmptyAbstractActor.class)
	ActorRef refA;

	@Test
	void test_injectSimpleActorRefByClass() {
		assertThat(refS).isNotNull();
	}

	@Test
	void test_injectAbstractActorRefByClass() {
		assertThat(refA).isNotNull();
	}
}
