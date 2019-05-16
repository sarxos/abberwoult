package com.github.sarxos.abberwoult;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.trait.Comm;
import com.github.sarxos.abberwoult.trait.Disposing;

import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class AskableActorSelectionTest {

	@Inject
	ActorSystemUniverse universe;

	@Inject
	@ActorOf(TestClass.class)
	AskableActorSelection ref;

	@Named("test")
	public static class TestClass extends SimpleActor implements Comm, Disposing {
		public void handleInteger(@Receives final Integer i) {
			reply(i);
		}
	}

	@BeforeEach
	public void setup() {
		universe.actor()
			.of(TestClass.class)
			.build();
	}

	@AfterEach
	public void teardown() {
		ref.tell(PoisonPill.getInstance());
	}

	@Test
	public void test_ask() throws Exception {

		final CompletionStage<Integer> result = ref.ask(22);

		Assertions
			.assertThat(result.toCompletableFuture().get(3, TimeUnit.SECONDS))
			.isEqualTo(22);
	}
}
