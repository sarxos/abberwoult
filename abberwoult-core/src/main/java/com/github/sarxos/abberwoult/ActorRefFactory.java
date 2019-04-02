package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getQualifier;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.sarxos.abberwoult.annotation.ActorByClass;
import com.github.sarxos.abberwoult.cdi.BeanInjectionException;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;


@Singleton
public class ActorRefFactory {

	private final Propser propser;
	private final ActorSystem system;

	@Inject
	public ActorRefFactory(final Propser propser, final ActorSystem system) {
		this.propser = propser;
		this.system = system;
	}

	@Produces
	@Dependent
	@ActorByClass
	public ActorRef create(final InjectionPoint injection) {

		if (injection == null) {
			throw new NullInjectionPointException();
		}

		final Class<? extends Actor> clazz = getActorClass(injection);
		final Props props = propser.props(clazz);

		return ActorUtils
			.getActorName(clazz)
			.map(name -> system.actorOf(props, name))
			.getOrElse(() -> system.actorOf(props));
	}

	private Class<? extends Actor> getActorClass(final InjectionPoint injection) {
		return getQualifier(injection, ActorByClass.class)
			.map(ActorByClass::value)
			.getOrElseThrow(() -> new MissingActorRefQualifierException(injection));
	}

	@SuppressWarnings("serial")
	public static class MissingActorRefQualifierException extends BeanInjectionException {
		public MissingActorRefQualifierException(final InjectionPoint ip) {
			super("Qualifier was not found for " + ip.getMember() + " of " + ip.getType());
		}
	}

	@SuppressWarnings("serial")
	public static class NullInjectionPointException extends BeanInjectionException {
		public NullInjectionPointException() {
			super("The " + ActorRef.class + " can only be injected into injection point");
		}
	}
}
