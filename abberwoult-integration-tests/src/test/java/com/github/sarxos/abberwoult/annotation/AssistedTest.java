package com.github.sarxos.abberwoult.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.annotation.AssistedTesting.AssistedActorWithFirstArgumentAnnotated;
import com.github.sarxos.abberwoult.annotation.AssistedTesting.AssistedActorWithLastArgumentAnnotated;
import com.github.sarxos.abberwoult.testkit.TestKit;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class AssistedTest {

	@Inject
	TestKit testkit;

	@Test
	public void test_assistedLast() {

		final Integer assisted = 3;

		final ActorRef ref = testkit.actor()
			.of(AssistedActorWithLastArgumentAnnotated.class)
			.withArguments(assisted)
			.create();

		final AssistedActorWithLastArgumentAnnotated actor = testkit.extract(ref);

		assertThat(actor.getService()).isNotNull();
		assertThat(actor.getAssisted()).isNotNull().isEqualTo(assisted);

		testkit.kill(ref);
	}

	@Test
	public void test_assistedFirst() {

		final Integer assisted = 4;

		final ActorRef ref = testkit.actor()
			.of(AssistedActorWithFirstArgumentAnnotated.class)
			.withArguments(assisted)
			.create();

		final AssistedActorWithFirstArgumentAnnotated actor = testkit.extract(ref);

		assertThat(actor.getService()).isNotNull();
		assertThat(actor.getAssisted()).isNotNull().isEqualTo(assisted);

		testkit.kill(ref);
	}
}
