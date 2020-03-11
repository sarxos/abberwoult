package com.github.sarxos.abberwoult.deployment.item;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;

import io.quarkus.builder.item.MultiBuildItem;


/**
 * A build item which reflects actor class.
 *
 * @author Bartosz Firyn (sarxos)
 */
public final class ActorBuildItem extends MultiBuildItem {

	private final ClassRef actorClass;

	public ActorBuildItem(final ClassRef actorClass) {
		this.actorClass = actorClass;
	}

	public ClassRef getActorClass() {
		return actorClass;
	}

	public String getActorClassName() {
		return getActorClass().getName();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
