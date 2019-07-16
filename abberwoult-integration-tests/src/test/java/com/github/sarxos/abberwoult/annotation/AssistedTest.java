package com.github.sarxos.abberwoult.annotation;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.testkit.TestKit;

import akka.actor.ActorRef;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class AssistedTest {

	@Singleton
	public static class TestService {
	}

	public static class TestActorAssistedLast extends SimpleActor {

		private final TestService service;
		private final Integer assisted;

		@Inject
		public TestActorAssistedLast(TestService service, @Assisted Integer assisted) {
			this.service = service;
			this.assisted = assisted;
		}

		public TestService getService() {
			return service;
		}

		public Integer getAssisted() {
			return assisted;
		}
	}

	public static class TestActorAssistedFirst extends SimpleActor {

		private final TestService service;
		private final Integer assisted;

		@Inject
		public TestActorAssistedFirst(@Assisted Integer assisted, TestService service) {
			this.service = service;
			this.assisted = assisted;
		}

		public TestService getService() {
			return service;
		}

		public Integer getAssisted() {
			return assisted;
		}
	}

	@Inject
	TestKit testkit;

	@Test
	public void test_assistedLast() {

		final Integer assisted = 3;

		final ActorRef ref = testkit.actor()
			.of(TestActorAssistedLast.class)
			.withArguments(assisted)
			.create();

		final TestActorAssistedLast actor = testkit.extract(ref);

		assertThat(actor.service).isNotNull();
		assertThat(actor.assisted).isNotNull().isEqualTo(3);

		testkit.kill(ref);
	}

	@Test
	public void test_assistedFirst() {

		final Integer assisted = 3;

		final ActorRef ref = testkit.actor()
			.of(TestActorAssistedFirst.class)
			.withArguments(assisted)
			.create();

		final TestActorAssistedFirst actor = testkit.extract(ref);

		assertThat(actor.service).isNotNull();
		assertThat(actor.assisted).isNotNull().isEqualTo(3);

		testkit.kill(ref);
	}
}
