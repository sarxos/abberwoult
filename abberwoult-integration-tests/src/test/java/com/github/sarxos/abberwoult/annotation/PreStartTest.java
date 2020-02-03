package com.github.sarxos.abberwoult.annotation;

import static akka.actor.ActorRef.noSender;
import static com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.getPreStartsFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.github.sarxos.abberwoult.ActorUniverse;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import io.quarkus.test.junit.QuarkusTest;


@QuarkusTest
public class PreStartTest {

	public static final class TestActorWithSinglePreStart extends SimpleActor {

		private final AtomicBoolean started;

		public TestActorWithSinglePreStart(final AtomicBoolean started) {
			this.started = started;
		}

		@PreStart
		public void setup() {
			started.set(true);
		}
	}

	public static final class TestActorWithMultiplePreStarts extends SimpleActor {

		private final AtomicBoolean started1;
		private final AtomicBoolean started2;

		public TestActorWithMultiplePreStarts(final AtomicBoolean started1, final AtomicBoolean started2) {
			this.started1 = started1;
			this.started2 = started2;
		}

		@PreStart
		public void setup1() {
			started1.set(true);
		}

		@PreStart
		public void setup2() {
			started2.set(true);
		}
	}

	public static abstract class TestSuperClassActorWithSinglePreStart extends SimpleActor {

		final AtomicBoolean started;

		public TestSuperClassActorWithSinglePreStart(final AtomicBoolean started) {
			this.started = started;
		}

		@PreStart
		public void setup() {
			started.set(true);
		}
	}

	public static final class TestSubClassActorWithSinglePreStart extends TestSuperClassActorWithSinglePreStart {

		public TestSubClassActorWithSinglePreStart(final AtomicBoolean started) {
			super(started);
		}
	}

	public static final class TestSubClassActorWithOverridenPreStart extends TestSuperClassActorWithSinglePreStart {

		final AtomicBoolean started2;

		public TestSubClassActorWithOverridenPreStart(final AtomicBoolean started1, final AtomicBoolean started2) {
			super(started1);
			this.started2 = started2;
		}

		@Override
		@PreStart
		public void setup() {
			started2.set(true);
		}
	}

	public static interface DummyInterface {

		static final AtomicBoolean started = new AtomicBoolean();

		@PreStart
		default void setup() {
			started.set(true);
		}
	}

	public static final class TestActorImplementingInterface extends SimpleActor implements DummyInterface {

	}

	@Inject
	ActorUniverse universe;

	@Test
	public void test_ifCorrectNumberOfPreStartsIsRegistered() {
		assertThat(getPreStartsFor(TestActorWithSinglePreStart.class)).hasSize(1);
		assertThat(getPreStartsFor(TestActorWithMultiplePreStarts.class)).hasSize(2);
		assertThat(getPreStartsFor(TestSubClassActorWithSinglePreStart.class)).hasSize(1);
		assertThat(getPreStartsFor(TestSubClassActorWithOverridenPreStart.class)).hasSize(1);
		assertThat(getPreStartsFor(TestActorImplementingInterface.class)).hasSize(1);
	}

	@Test
	public void test_ifSinglePreStartIsInvoked() {

		final AtomicBoolean started = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestActorWithSinglePreStart.class)
			.withArguments(started)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(started);
	}

	@Test
	public void test_ifMultiplePreStartsAreInvoked() {

		final AtomicBoolean started1 = new AtomicBoolean(false);
		final AtomicBoolean started2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestActorWithMultiplePreStarts.class)
			.withArguments(started1, started2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(started1);
		await().untilTrue(started2);
	}

	@Test
	public void test_ifSinglePreStartIsInvokedInSuperclass() {

		final AtomicBoolean started = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestSubClassActorWithSinglePreStart.class)
			.withArguments(started)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(started);
	}

	@Test
	public void test_ifSinglePreStartIsInvokedWhenOverride() {

		final AtomicBoolean started1 = new AtomicBoolean(false);
		final AtomicBoolean started2 = new AtomicBoolean(false);

		final ActorRef ref = universe.actor()
			.of(TestSubClassActorWithOverridenPreStart.class)
			.withArguments(started1, started2)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(started2);

		assertThat(started1).isFalse();
	}

	@Test
	public void test_ifSinglePreStartIsInvokedInInterface() {

		DummyInterface.started.set(false);

		final ActorRef ref = universe.actor()
			.of(TestActorImplementingInterface.class)
			.create();

		ref.tell(PoisonPill.getInstance(), noSender());

		await().untilTrue(DummyInterface.started);
	}
}
