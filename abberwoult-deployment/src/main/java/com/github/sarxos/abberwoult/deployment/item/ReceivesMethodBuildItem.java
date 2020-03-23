package com.github.sarxos.abberwoult.deployment.item;

import com.github.sarxos.abberwoult.jandex.Reflector.ParameterRef;

import io.quarkus.builder.item.MultiBuildItem;


public final class ReceivesMethodBuildItem extends MultiBuildItem {

	private final ParameterRef pr;

	public ReceivesMethodBuildItem(final ParameterRef pr) {
		this.pr = pr;
	}
}
