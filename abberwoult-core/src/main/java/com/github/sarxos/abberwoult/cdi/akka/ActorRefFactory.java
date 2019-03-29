package com.github.sarxos.abberwoult.cdi.akka;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getQualifier;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.github.sarxos.abberwoult.Propser;
import com.github.sarxos.abberwoult.annotation.ByClass;
import com.github.sarxos.abberwoult.cdi.BeanInjectionException;
import com.github.sarxos.abberwoult.util.ActorUtils;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;


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
	public ActorRef create(final InjectionPoint injection) {

		if (injection == null) {
			System.out.println("byuoll");
			return ActorRef.noSender();
			// throw new IllegalStateException("Null injection point");
		} else {
			System.out.println(ToStringBuilder.reflectionToString(injection));
		}

		final Class<? extends Actor> clazz = getActorClass(injection);
		final Props props = propser.props(clazz);

		return ActorUtils
			.getActorName(clazz)
			.map(name -> system.actorOf(props, name))
			.getOrElse(() -> system.actorOf(props));
	}

	private Class<? extends Actor> getActorClass(final InjectionPoint injection) {
		return getQualifier(injection, ByClass.class)
			.peek(q -> System.out.println(q))
			.map(ByClass::value)
			.getOrElseThrow(() -> new MissingActorRefQualifierException(injection));
	}

	@SuppressWarnings("serial")
	public static class MissingActorRefQualifierException extends BeanInjectionException {
		public MissingActorRefQualifierException(final InjectionPoint ip) {
			super("Qualifier was not found for " + ip.getMember() + " of " + ip.getType());
		}
	}
}
