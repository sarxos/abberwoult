package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.sarxos.abberwoult.SimpleActor;


public class PreStartTesting {

	public static interface PreStartInterfaceWithSingleBinding {

		static final AtomicInteger started = new AtomicInteger();

		@PreStart
		default void setup() {
			started.incrementAndGet();
		}
	}

	public static interface PreStartInterfaceWithMultipleBindings {

		static final AtomicBoolean started = new AtomicBoolean();

		@PreStart
		default void setup() {
			started.set(true);
		}
	}

	public static final class PreStartActorWithSingleBinding extends SimpleActor {

		private final AtomicBoolean started;

		public PreStartActorWithSingleBinding(final AtomicBoolean started) {
			this.started = started;
		}

		@PreStart
		public void setup() {
			started.set(true);
		}
	}

	public static final class PreStartActorWithMultipleBindings extends SimpleActor {

		private final AtomicBoolean started1;
		private final AtomicBoolean started2;

		public PreStartActorWithMultipleBindings(final AtomicBoolean started1, final AtomicBoolean started2) {
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

	public static final class PreStartActorSubClassWithSingleBinding extends PreStartActorAbstractClassWithSingleBinding {

		public PreStartActorSubClassWithSingleBinding(final AtomicBoolean started) {
			super(started);
		}
	}

	public static final class PreStartActorSubClassWithOverridenBinding extends PreStartActorAbstractClassWithSingleBinding {

		final AtomicBoolean started2;

		public PreStartActorSubClassWithOverridenBinding(final AtomicBoolean started1, final AtomicBoolean started2) {
			super(started1);
			this.started2 = started2;
		}

		@Override
		@PreStart
		public void setup() {
			started2.set(true);
		}
	}

	public static final class PreStartActorImplementingInterface extends SimpleActor implements PreStartInterfaceWithSingleBinding {

	}

	public static abstract class PreStartActorAbstractClassWithSingleBinding extends SimpleActor {

		final AtomicBoolean started;

		public PreStartActorAbstractClassWithSingleBinding(final AtomicBoolean started) {
			this.started = started;
		}

		@PreStart
		public void setup() {
			started.set(true);
		}
	}
}
