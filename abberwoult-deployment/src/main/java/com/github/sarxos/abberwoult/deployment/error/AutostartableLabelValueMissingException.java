package com.github.sarxos.abberwoult.deployment.error;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.deployment.item.ActorBuildItem;


@SuppressWarnings("serial")
public class AutostartableLabelValueMissingException extends IllegalArgumentException {

	public AutostartableLabelValueMissingException(final ActorBuildItem item) {
		super(""
			+ "Actor class " + item.getActorClassName() + " is annotated with " + Autostart.class + " "
			+ "and " + Named.class + " but label value is missing");
	}
}
