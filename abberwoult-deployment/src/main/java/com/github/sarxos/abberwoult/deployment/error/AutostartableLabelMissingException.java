package com.github.sarxos.abberwoult.deployment.error;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.deployment.item.ActorBuildItem;


@SuppressWarnings("serial")
public class AutostartableLabelMissingException extends IllegalArgumentException {

	public AutostartableLabelMissingException(final ActorBuildItem item) {
		super(""
			+ "Actor class " + item.getActorClassName() + " is annotated with " + Autostart.class + " "
			+ "and therefore require " + Labeled.class + " annotation to be present on type, but no such "
			+ "annotation has been found");
	}
}
