package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.PreStart;


public abstract class PreStartAbstractActorClassWithSingleBinding extends SimpleActor {

	final AtomicBoolean started;

	public PreStartAbstractActorClassWithSingleBinding(final AtomicBoolean started) {
		this.started = started;
	}

	@PreStart
	public void setup() {
		started.set(true);
	}
}
