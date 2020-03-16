package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.SimpleActor;


public final class PostStopTesting {

	public static final class PostStopActorWithSingleBinding extends SimpleActor {

		private final AtomicBoolean teardowned;

		public PostStopActorWithSingleBinding(final AtomicBoolean teardowned) {
			this.teardowned = teardowned;
		}

		@PostStop
		public void teardown() {
			teardowned.set(true);
		}
	}

	public static final class PostStopActorWithMultipleBindings extends SimpleActor {

		private final AtomicBoolean teardowned1;
		private final AtomicBoolean teardowned2;

		public PostStopActorWithMultipleBindings(final AtomicBoolean teardowned1, final AtomicBoolean teardowned2) {
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

	public static abstract class PostStopActorAbstractClassWithSingleBinding extends SimpleActor {

		final AtomicBoolean teardowned;

		public PostStopActorAbstractClassWithSingleBinding(final AtomicBoolean teardowned) {
			this.teardowned = teardowned;
		}

		@PostStop
		public void teardown() {
			teardowned.set(true);
		}
	}

	public static final class PostStopActorSubClassWithSingleBinding extends PostStopActorAbstractClassWithSingleBinding {

		public PostStopActorSubClassWithSingleBinding(final AtomicBoolean teardowned) {
			super(teardowned);
		}
	}

	public static final class PostStopActorSubClassWithOverridenBinding extends PostStopActorAbstractClassWithSingleBinding {

		final AtomicBoolean teardowned2;

		public PostStopActorSubClassWithOverridenBinding(final AtomicBoolean teardowned1, final AtomicBoolean teardowned2) {
			super(teardowned1);
			this.teardowned2 = teardowned2;
		}

		@Override
		@PostStop
		public void teardown() {
			teardowned2.set(true);
		}
	}

	public static interface PostStopInterfaceWithSingleBinding {

		static final AtomicBoolean teardowned = new AtomicBoolean();

		@PostStop
		default void teardown() {
			teardowned.set(true);
		}
	}

	public static final class PostStopActorImplementingInterface extends SimpleActor implements PostStopInterfaceWithSingleBinding {

	}
}
