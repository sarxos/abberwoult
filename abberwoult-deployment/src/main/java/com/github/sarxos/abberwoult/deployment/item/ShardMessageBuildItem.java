package com.github.sarxos.abberwoult.deployment.item;

import org.jboss.jandex.ClassInfo;

import io.quarkus.builder.item.MultiBuildItem;


/**
 * A build item which reflects shard routable message.
 *
 * @author Bartosz Firyn (sarxos)
 */
final public class ShardMessageBuildItem extends MultiBuildItem {

	private final ClassInfo messageClass;

	public ShardMessageBuildItem(final ClassInfo messageClass) {
		this.messageClass = messageClass;
	}

	public ClassInfo getMessageClass() {
		return messageClass;
	}
}
