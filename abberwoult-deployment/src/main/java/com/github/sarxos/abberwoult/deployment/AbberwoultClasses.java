package com.github.sarxos.abberwoult.deployment;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import org.jboss.jandex.DotName;

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.ActorScoped;
import com.github.sarxos.abberwoult.annotation.Assisted;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.annotation.PreStart;


public class AbberwoultClasses {

	public static final DotName ACTOR_SCOPED_ANNOTATION = DotName.createSimple(ActorScoped.class.getName());
	public static final DotName APPLICATION_SCOPED_ANNOTATION = DotName.createSimple(ApplicationScoped.class.getName());
	public static final DotName MESSAGE_HANDLER_ANNOTATION = DotName.createSimple(Receives.class.getName());
	public static final DotName PRE_START_ANNOTATION = DotName.createSimple(PreStart.class.getName());
	public static final DotName INJECT_ANNOTATION = DotName.createSimple(Inject.class.getName());
	public static final DotName VALID_ANNOTATION = DotName.createSimple(Valid.class.getName());
	public static final DotName ASSISTED_ANNOTATION = DotName.createSimple(Assisted.class.getName());
	public static final DotName SIMPLE_ACTOR_CLASS = DotName.createSimple(SimpleActor.class.getName());

	private AbberwoultClasses() {
		// class enum
	}
}
