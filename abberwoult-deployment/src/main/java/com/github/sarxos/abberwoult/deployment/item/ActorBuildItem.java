package com.github.sarxos.abberwoult.deployment.item;

import org.jboss.jandex.ClassInfo;

import io.quarkus.builder.item.MultiBuildItem;


/**
 * A build item which reflects actor class.
 *
 * @author Bartosz Firyn (sarxos)
 */
final public class ActorBuildItem extends MultiBuildItem {

	private final ClassInfo actorClass;

	public ActorBuildItem(final ClassInfo actorClass) {
		this.actorClass = actorClass;
	}

	public ClassInfo getActorClass() {
		return actorClass;
	}

	public String getActorClassName() {
		return actorClass.name().toString();
	}
}
