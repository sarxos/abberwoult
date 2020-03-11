package com.github.sarxos.abberwoult.annotation;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.EmptyService;
import com.github.sarxos.abberwoult.SimpleActor;


public class AssistedActorWithFirstArgumentAnnotated extends SimpleActor {

	private final EmptyService service;
	private final Integer assisted;

	@Inject
	public AssistedActorWithFirstArgumentAnnotated(@Assisted Integer assisted, EmptyService service) {
		this.service = service;
		this.assisted = assisted;
	}

	public EmptyService getService() {
		return service;
	}

	public Integer getAssisted() {
		return assisted;
	}
}
