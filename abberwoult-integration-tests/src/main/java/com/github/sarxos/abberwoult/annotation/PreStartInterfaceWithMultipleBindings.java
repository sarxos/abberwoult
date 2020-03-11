package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.abberwoult.annotation.PreStart;


public interface PreStartInterfaceWithMultipleBindings {

	static final AtomicBoolean started = new AtomicBoolean();

	@PreStart
	default void setup() {
		started.set(true);
	}
}
