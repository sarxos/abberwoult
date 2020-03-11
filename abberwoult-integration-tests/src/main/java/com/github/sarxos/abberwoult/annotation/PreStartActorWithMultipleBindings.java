package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;


public final class PreStartActorWithMultipleBindings extends SimpleActor {

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
