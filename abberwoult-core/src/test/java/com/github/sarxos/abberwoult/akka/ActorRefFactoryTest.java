package com.github.sarxos.abberwoult.akka;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.ByClass;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class ActorRefFactoryTest {

	static final class TestActor extends SimpleActor {
	}

	@Inject
	@ByClass(TestActor.class)
	ActorRef ref;

	@Test
	void test_injectActorRefByClass() {
		assertThat(ref).isNotNull();
	}
}
