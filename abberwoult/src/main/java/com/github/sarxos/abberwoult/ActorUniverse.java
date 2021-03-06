package com.github.sarxos.abberwoult;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.Validator;

import com.github.sarxos.abberwoult.builder.ActorBuilder;
import com.github.sarxos.abberwoult.builder.SingletonBuilder;
import com.github.sarxos.abberwoult.dsl.Selectors;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.EventStream;


/**
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class ActorUniverse implements Selectors {

	private final ActorSystem system;
	private final Propser propser;
	private final Validator validator;

	@Inject
	public ActorUniverse(final ActorSystem system, final Propser propser, final Validator validator) {
		this.system = system;
		this.propser = propser;
		this.validator = validator;
	}

	/**
	 * @return A {@link Propser} entity designed to create actor {@link Props}
	 */
	public Propser propser() {
		return propser;
	}

	public Props props(final Class<? extends Actor> clazz, final Object... args) {
		return propser().props(clazz, args);
	}

	/**
	 * @return The {@link ActorSystem}
	 */
	@Override
	public ActorSystem system() {
		return system;
	}

	public Validator validator() {
		return validator;
	}

	/**
	 * @return New actor builder
	 */
	public ActorBuilder<?> actor() {
		return new ActorBuilder<>(this);
	}

	public SingletonBuilder<?> singleton() {
		return new SingletonBuilder<>(this);
	}

	public EventStream getEventStream() {
		return system.getEventStream();
	}

	public void subscribeEvent(final ActorRef ref, final Class<?> eventClass) {
		getEventStream().subscribe(ref, eventClass);
	}
}
