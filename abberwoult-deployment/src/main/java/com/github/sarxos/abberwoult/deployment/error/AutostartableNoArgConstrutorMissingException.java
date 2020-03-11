package com.github.sarxos.abberwoult.deployment.error;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.deployment.item.InstrumentedActorBuildItem;


@SuppressWarnings("serial")
public class AutostartableNoArgConstrutorMissingException extends IllegalArgumentException {

	public AutostartableNoArgConstrutorMissingException(final InstrumentedActorBuildItem item) {
		super(""
			+ "Actor class " + item.getActorClassName() + " is annotated with " + Autostart.class + " "
			+ "and therefore require no-arg constructor to be present, but no such constructor has "
			+ "been found");
	}
}
