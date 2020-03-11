package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicInteger;


public interface PreStartInterfaceWithSingleBinding {

	static final AtomicInteger started = new AtomicInteger();

	@PreStart
	default void setup() {
		started.incrementAndGet();
	}
}
