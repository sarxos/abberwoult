package com.github.sarxos.abberwoult.annotation;

import javax.inject.Inject;
import javax.inject.Provider;

import com.github.sarxos.abberwoult.EmptyService;
import com.github.sarxos.abberwoult.SimpleActor;


public class AssistedTesting {

	public static class InjectProviderActor extends SimpleActor {

		@Inject
		EmptyService service;

		@Inject
		Provider<EmptyService> provider;
	}

	public static class AssistedActorWithFirstArgumentAnnotated extends SimpleActor {

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

	public static class AssistedActorWithLastArgumentAnnotated extends SimpleActor {

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
}
