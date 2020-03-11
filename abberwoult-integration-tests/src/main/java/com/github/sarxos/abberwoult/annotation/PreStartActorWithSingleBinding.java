package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;


public final class PreStartActorWithSingleBinding extends SimpleActor {

	private final AtomicBoolean started;

	public PreStartActorWithSingleBinding(final AtomicBoolean started) {
		this.started = started;
	}

	@PreStart
	public void setup() {
		started.set(true);
	}
}
