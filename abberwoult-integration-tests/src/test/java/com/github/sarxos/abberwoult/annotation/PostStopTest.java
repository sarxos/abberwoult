package com.github.sarxos.abberwoult.annotation;

import static akka.actor.ActorRef.noSender;
import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getPostStopsFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PostStop;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class PostStopTest {

	public static final class TestActorWithSinglePostStop extends SimpleActor {

		private final AtomicBoolean teardowned;

		public TestActorWithSinglePostStop(final AtomicBoolean teardowned) {
			this.teardowned = teardowned;
		}

		@PostStop
		public void teardown() {
			teardowned.set(true);
		}
	}

	public static final class TestActorWithMultiplePostStops extends SimpleActor {

		private final AtomicBoolean teardowned1;
		private final AtomicBoolean teardowned2;

		public TestActorWithMultiplePostStops(final AtomicBoolean teardowned1, final AtomicBoolean teardowned2) {
			this.teardowned1 = teardowned1;
			this.teardowned2 = teardowned2;
		}

		@PostStop
		public void teardown1() {
			teardowned1.set(true);
		}

		@PostStop
		public void teardowned2() {
			teardowned2.set(true);
		}
	}

	public static abstract class TestSuperClassActorWithSinglePostStop extends SimpleActor {

		final AtomicBoolean teardowned;

		public TestSuperClassActorWithSinglePostStop(final AtomicBoolean teardowned) {
			this.teardowned = teardowned;
		}

		@PostStop
		public void teardown() {
			teardowned.set(true);
		}
	}

	public static final class TestSubClassActorWithSinglePostStop extends TestSuperClassActorWithSinglePostStop {

		public TestSubClassActorWithSinglePostStop(final AtomicBoolean teardowned) {
			super(teardowned);
		}
	}

	public static final class TestSubClassActorWithOverridenPostStop extends TestSuperClassActorWithSinglePostStop {

		final AtomicBoolean teardowned2;

		public TestSubClassActorWithOverridenPostStop(final AtomicBoolean teardowned1, final AtomicBoolean teardowned2) {
			super(teardowned1);
			this.teardowned2 = teardowned2;
		}

		@Override
		@PostStop
		public void teardown() {
			teardowned2.set(true);
		}
	}

	public static interface DummyInterface {

		static final AtomicBoolean teardowned = new AtomicBoolean();

		@PostStop
		default void teardown() {
			teardowned.set(true);
		}
	}

	public static final class TestActorImplementingInterface extends SimpleActor implements DummyInterface {

	}

	@Inject
	ActorUniverse universe;

	@Test
	public void test_ifCorrectNumberOfPostStopsIsRegistered() {
		assertThat(getPostStopsFor(TestActorWithSinglePostStop.class)).hasSize(1);
		assertThat(getPostStopsFor(TestActorWithMultiplePostStops.class)).hasSize(2);
		assertThat(getPostStopsFor(TestSubClassActorWithSinglePostStop.class)).hasSize(1);
		assertThat(getPostStopsFor(TestSubClassActorWithOverridenPostStop.class)).hasSize(1);
		assertThat(getPostStopsFor(TestActorImplementingInterface.class)).hasSize(1);
	}

	@Test
	public void test_ifSinglePostStopIsInvoked() {

		final AtomicBoolean teardowned = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestActorWithSinglePostStop.class)
			.withArguments(teardowned)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned);
	}

	@Test
	public void test_ifMultiplePostStopsAreInvoked() {

		final AtomicBoolean teardowned1 = new AtomicBoolean(false);
		final AtomicBoolean teardowned2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestActorWithMultiplePostStops.class)
			.withArguments(teardowned1, teardowned2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned1);
		await().untilTrue(teardowned2);
	}

	@Test
	public void test_ifSinglePreStartIsInvokedInSuperclass() {

		final AtomicBoolean teardowned = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestSubClassActorWithSinglePostStop.class)
			.withArguments(teardowned)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned);
	}

	@Test
	public void test_ifSinglePostStopIsInvokedWhenOverride() {

		final AtomicBoolean teardowned1 = new AtomicBoolean(false);
		final AtomicBoolean teardowned2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestSubClassActorWithOverridenPostStop.class)
			.withArguments(teardowned1, teardowned2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(teardowned2);

		assertThat(teardowned1).isFalse();
	}

	@Test
	public void test_ifSinglePostStopIsInvokedInInterface() {

		DummyInterface.teardowned.set(false);

		final ActorRef ref = universe.actor()
			.of(TestActorImplementingInterface.class)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(DummyInterface.teardowned);
	}
}
