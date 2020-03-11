package com.github.sarxos.abberwoult.deployment.error;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.deployment.item.InstrumentedActorBuildItem;


@SuppressWarnings("serial")
public class AutostartableNameMissingException extends IllegalArgumentException {

	public AutostartableNameMissingException(final InstrumentedActorBuildItem item) {
		super(""
			+ "Actor class " + item.getActorClassName() + " is annotated with " + Autostart.class + " "
			+ "and therefore require " + Named.class + " annotation to be present on type, but no such "
			+ "annotation has been found or it has no value defined");
	}
}
