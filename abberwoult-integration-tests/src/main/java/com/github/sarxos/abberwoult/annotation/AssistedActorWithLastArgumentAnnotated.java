package com.github.sarxos.abberwoult.annotation;

import javax.inject.Inject;

import com.github.sarxos.abberwoult.EmptyService;
import com.github.sarxos.abberwoult.SimpleActor;


public class AssistedActorWithLastArgumentAnnotated extends SimpleActor {

	private final EmptyService service;
	private final Integer assisted;

	@Inject
	public AssistedActorWithLastArgumentAnnotated(EmptyService service, @Assisted Integer assisted) {
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
