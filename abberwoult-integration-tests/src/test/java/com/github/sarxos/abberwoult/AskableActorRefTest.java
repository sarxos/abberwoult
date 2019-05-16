package com.github.sarxos.abberwoult;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.ActorOf;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.trait.Comm;
import com.github.sarxos.abberwoult.trait.Disposing;

import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class AskableActorRefTest {

	@Inject
	@ActorOf(TestClass.class)
	AskableActorRef ref;

	public static class TestClass extends SimpleActor implements Comm, Disposing {
		public void handleInteger(@Receives final Integer i) {
			reply(i);
			dispose();
		}
	}

	@Test
	public void test_ask() throws Exception {

		final CompletionStage<Integer> result = ref.ask(22);

		Assertions
			.assertThat(result.toCompletableFuture().get(3, TimeUnit.SECONDS))
			.isEqualTo(22);
	}
}
