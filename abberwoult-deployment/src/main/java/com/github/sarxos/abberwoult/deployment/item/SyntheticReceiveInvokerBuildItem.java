package com.github.sarxos.abberwoult.deployment.item;

import java.util.Map;

import com.github.sarxos.abberwoult.deployment.util.ReceiveInvokerGenerator;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;

import io.quarkus.builder.item.MultiBuildItem;


/**
 * @author Bartosz Firyn (sarxos)
 */
public final class SyntheticReceiveInvokerBuildItem extends MultiBuildItem {

	private static final ReceiveInvokerGenerator GENERATOR = new ReceiveInvokerGenerator();

	private final ClassRef actorClass;
	private final Map<String, byte[]> invokers;

	public SyntheticReceiveInvokerBuildItem(final ClassRef actorClass) {
		this.actorClass = actorClass;
		this.invokers = GENERATOR.generate(actorClass);
	}

	public Map<String, byte[]> getInvokers() {
		return invokers;
	}

	public ClassRef getActorClass() {
		return actorClass;
	}
}
