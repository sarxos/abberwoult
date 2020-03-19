package com.github.sarxos.abberwoult.annotation;

import static akka.actor.ActorRef.noSender;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.awaitility.Duration;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.annotation.PreStartTesting.PreStartActorAbstractClassWithSingleBinding;
import com.github.sarxos.abberwoult.annotation.PreStartTesting.PreStartActorImplementingInterface;
import com.github.sarxos.abberwoult.annotation.PreStartTesting.PreStartActorSubClassWithOverridenBinding;
import com.github.sarxos.abberwoult.annotation.PreStartTesting.PreStartActorSubClassWithSingleBinding;
import com.github.sarxos.abberwoult.annotation.PreStartTesting.PreStartActorWithMultipleBindings;
import com.github.sarxos.abberwoult.annotation.PreStartTesting.PreStartActorWithSingleBinding;
import com.github.sarxos.abberwoult.annotation.PreStartTesting.PreStartInterfaceWithSingleBinding;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class PreStartTest {

	@Inject
	ActorUniverse universe;

	@Test
	public void test_preStartActorWithSingleBinding() {

		final AtomicBoolean started = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PreStartActorWithSingleBinding.class)
			.withArguments(started)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().atMost(Duration.ONE_SECOND).untilTrue(started);
	}

	@Test
	public void test_preStartActorWithMultipleBindings() {

		final AtomicBoolean started1 = new AtomicBoolean(false);
		final AtomicBoolean started2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PreStartActorWithMultipleBindings.class)
			.withArguments(started1, started2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().atMost(Duration.ONE_SECOND).untilTrue(started1);
		await().atMost(Duration.ONE_SECOND).untilTrue(started2);
	}

	@Test
	public void test_preStartActorSubClassWithSingleBinding() {

		final AtomicBoolean started = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PreStartActorSubClassWithSingleBinding.class)
			.withArguments(started)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().atMost(Duration.ONE_SECOND).untilTrue(started);
	}

	@Test
	public void test_preStartActorSubClassWithOverridenBinding() {

		final AtomicBoolean started1 = new AtomicBoolean(false);
		final AtomicBoolean started2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(PreStartActorSubClassWithOverridenBinding.class)
			.withArguments(started1, started2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().atMost(Duration.ONE_SECOND).untilTrue(started2);

		assertThat(started1).isFalse();
	}

	@Test
	public void test_preStartActorImplementingInterface() {

		final ActorRef ref = universe.actor()
			.of(PreStartActorImplementingInterface.class)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().atMost(Duration.ONE_SECOND).untilAtomic(PreStartInterfaceWithSingleBinding.started, Matchers.is(1));
	}

	@Test
	public void test_preStartMethodNotGeneratedForAbstractClass() {
		Assertions.assertThrows(NoSuchMethodException.class, () -> {
			PreStartActorAbstractClassWithSingleBinding.class.getDeclaredMethod("preStart");
		});
	}

	@Test
	public void test_preStartMethodGeneratedForNonAbstractClass() throws NoSuchMethodException, SecurityException {
		Assertions.assertNotNull(PreStartActorSubClassWithSingleBinding.class.getDeclaredMethod("preStart"));
	}
}
