package com.github.sarxos.abberwoult.deployment.item;

import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;

import io.quarkus.builder.item.MultiBuildItem;


/**
 * A build item which reflects shard routable message.
 *
 * @author Bartosz Firyn (sarxos)
 */
final public class ShardMessageBuildItem extends MultiBuildItem {

	private final ClassRef messageClass;

	public ShardMessageBuildItem(final ClassRef messageClass) {
		this.messageClass = messageClass;
	}

	public ClassRef getMessageClass() {
		return messageClass;
	}
}
