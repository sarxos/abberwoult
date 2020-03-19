package com.github.sarxos.abberwoult.builder;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.dsl.Utils;


public class SingletonBuilderTesting {

	public static class SingletonActor extends SimpleActor implements Utils {

		final AtomicBoolean started;
		final AtomicBoolean disposed;

		public SingletonActor(final AtomicBoolean started, final AtomicBoolean disposes) {
			this.started = started;
			this.disposed = disposes;
		}

		@PreStart
		public void setup() {
			started.set(true);
		}

		@PostStop
		public void teardown() {
			disposed.set(true);
		}
	}
}
