package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getQualifier;

import javax.enterprise.inject.spi.InjectionPoint;

import com.github.sarxos.abberwoult.annotation.ActorOf;

import akka.actor.Actor;
import akka.actor.ActorRef;


public abstract class AbstractInjectFactory {

	protected Class<? extends Actor> getActorClass(final InjectionPoint injection) {
		return getQualifier(injection, ActorOf.class)
			.map(ActorOf::value)
			.filter(this::isValidActorClass)
			.getOrElseThrow(() -> new NoActorClassProvidedException(injection));
	}

	private boolean isValidActorClass(final Class<? extends Actor> clazz) {
		return clazz != ActorOf.NoActor.class;
	}

	@SuppressWarnings("serial")
	public static class NoActorClassProvidedException extends BeanInjectionException {
		public NoActorClassProvidedException(final InjectionPoint injection) {
			super("No actor class was provided for " + injection.getMember());
		}
	}

	@SuppressWarnings("serial")
	public static class NullInjectionPointException extends BeanInjectionException {
		public NullInjectionPointException() {
			super("The " + ActorRef.class + " can only be injected into injection point");
		}
	}
}
