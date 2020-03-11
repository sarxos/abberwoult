package com.github.sarxos.abberwoult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import org.jboss.jandex.DotName;

import com.github.sarxos.abberwoult.ShardMessageExtractor.FieldReader;
import com.github.sarxos.abberwoult.annotation.ActorScoped;
import com.github.sarxos.abberwoult.annotation.Assisted;
import com.github.sarxos.abberwoult.annotation.Autostart;
import com.github.sarxos.abberwoult.annotation.Event;
import com.github.sarxos.abberwoult.annotation.Instrumented;
import com.github.sarxos.abberwoult.annotation.Named;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.annotation.ShardEntityId;
import com.github.sarxos.abberwoult.annotation.ShardId;

import akka.actor.AbstractActor;


public class DotNames {

	public static final DotName INSTRUMENTED_ANNOTATION = DotName.createSimple(Instrumented.class.getName());
	public static final DotName AUTOSTART_ANNOTATION = DotName.createSimple(Autostart.class.getName());
	public static final DotName ACTOR_SCOPED_ANNOTATION = DotName.createSimple(ActorScoped.class.getName());
	public static final DotName APPLICATION_SCOPED_ANNOTATION = DotName.createSimple(ApplicationScoped.class.getName());
	public static final DotName RECEIVES_ANNOTATION = DotName.createSimple(Receives.class.getName());
	public static final DotName PRE_START_ANNOTATION = DotName.createSimple(PreStart.class.getName());
	public static final DotName POST_STOP_ANNOTATION = DotName.createSimple(PostStop.class.getName());
	public static final DotName INJECT_ANNOTATION = DotName.createSimple(Inject.class.getName());
	public static final DotName VALID_ANNOTATION = DotName.createSimple(Valid.class.getName());
	public static final DotName ASSISTED_ANNOTATION = DotName.createSimple(Assisted.class.getName());
	public static final DotName EVENT_ANNOTATION = DotName.createSimple(Event.class.getName());
	public static final DotName NAMED_ANNOTATION = DotName.createSimple(Named.class.getName());
	public static final DotName SHARD_ID_ANNOTATION = DotName.createSimple(ShardId.class.getName());
	public static final DotName SHARD_ENTITY_ID_ANNOTATION = DotName.createSimple(ShardEntityId.class.getName());
	public static final DotName ABSTRACT_ACTOR_CLASS = DotName.createSimple(AbstractActor.class.getName());
	public static final DotName SIMPLE_ACTOR_CLASS = DotName.createSimple("com.github.sarxos.abberwoult.SimpleActor");
	public static final DotName SHARD_ROUTABLE_MESSAGE_INTERFACE = DotName.createSimple(ShardRoutableMessage.class.getName());
	public static final DotName FIELD_READER_INTERFACE = DotName.createSimple(FieldReader.class.getName());

	private DotNames() {
		// class enum
	}
}
