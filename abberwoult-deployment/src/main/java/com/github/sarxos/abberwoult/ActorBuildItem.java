package com.github.sarxos.abberwoult;

import org.jboss.jandex.ClassInfo;

import io.quarkus.builder.item.MultiBuildItem;


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
