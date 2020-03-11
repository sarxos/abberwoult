package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.annotation.PreStart;


public final class PreStartActorSubClassWithOverridenBinding extends PreStartAbstractActorClassWithSingleBinding {

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
