package com.github.sarxos.abberwoult.deployment.item;

import java.lang.annotation.Annotation;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jboss.jandex.DotName;

import com.github.sarxos.abberwoult.annotation.NamedActor;
import com.github.sarxos.abberwoult.deployment.util.ActorInstrumentor;
import com.github.sarxos.abberwoult.jandex.Reflector.AnnotationRef;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import io.vavr.control.Option;


/**
 * A build item which corresponds to instrumented actor class.
 *
 * @author Bartosz Firyn (sarxos)
 */
public final class InstrumentedActorBuildItem extends MultiBuildItem {

	private static final ActorInstrumentor INSTRUMENTOR = new ActorInstrumentor();

	private final ClassRef actorClass;
	private final byte[] bytecode;

	public InstrumentedActorBuildItem(final ClassRef actorClass) {
		this.actorClass = actorClass;
		this.bytecode = INSTRUMENTOR.instrument(actorClass);
	}

	public String getActorClassName() {
		return actorClass.getName();
	}

	public byte[] getBytecode() {
		return bytecode;
	}

	public boolean hasAnnotation(final DotName dn) {
		return actorClass.hasAnnotation(dn);
	}

	public Option<AnnotationRef> getAnnotation(final DotName dn) {
		return actorClass.getAnnotation(dn);
	}

	public <A extends Annotation> Option<A> getAnnotation(final Class<A> clazz) {
		return actorClass.getAnnotation(clazz);
	}

	public boolean hasNoArgsConstructor() {
		return actorClass.hasNoArgsConstructor();
	}

	public Option<String> getActorName() {
		return getAnnotation(NamedActor.class)
			.map(NamedActor::value);
	}

	public boolean isNamed() {
		return getActorName()
			.filter(name -> ObjectUtils.notEqual(name, NamedActor.UNKNOWN))
			.isDefined();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public ReflectiveClassBuildItem toReflectiveClassBuildItem() {
		return new ReflectiveClassBuildItem(true, true, getActorClassName());
	}
}
