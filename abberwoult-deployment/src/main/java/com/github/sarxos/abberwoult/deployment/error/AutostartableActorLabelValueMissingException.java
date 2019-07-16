package com.github.sarxos.abberwoult.deployment.error;

import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.deployment.item.ActorBuildItem;


@SuppressWarnings("serial")
public class AutostartableActorLabelValueMissingException extends IllegalArgumentException {

	public AutostartableActorLabelValueMissingException(final ActorBuildItem item) {
		super(""
			+ "Actor class " + item.getActorClassName() + " is annotated with " + Autostart.class + " "
			+ "and " + Labeled.class + " but label value is missing");
	}
}
