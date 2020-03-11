package com.github.sarxos.abberwoult.annotation;

import java.util.concurrent.atomic.AtomicBoolean;


public final class PreStartActorSubClassWithSingleBinding extends PreStartAbstractActorClassWithSingleBinding {

	public PreStartActorSubClassWithSingleBinding(final AtomicBoolean started) {
		super(started);
	}
}
