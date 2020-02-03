package com.github.sarxos.abberwoult.deployment.item;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;

import akka.japi.Creator;
import io.quarkus.builder.item.MultiBuildItem;
import io.vavr.control.Option;
import javassist.CannotCompileException;
import javassist.CtClass;


public final class SyntheticActorCreatorBuildItem extends MultiBuildItem {

	// private static final CreatorGenerator GENERATOR = new CreatorGenerator();

	// private final String actorClassName;
	// private final String syntheticClassName;
	// private final Creator<?> creator;
	// private final byte[] bytecode;

	public SyntheticActorCreatorBuildItem(final ClassInfo actorClass) {

		// final CtClass cc = GENERATOR.generate(actorClass);

		// this.actorClassName = actorClass.name().toString();
		// this.syntheticClassName = cc.getName();
		// this.creator = createCreator(cc);
		// this.bytecode = toBytecode(cc);
	}

	private static Creator<?> createCreator(final CtClass cc) {
		return Option.of(cc).toTry()
			.mapTry(CtClass::toClass)
			.mapTry(Class::newInstance)
			.map(Creator.class::cast)
			.get();
	}

	private static byte[] toBytecode(final CtClass cc) {
		try {
			return cc.toBytecode();
		} catch (IOException | CannotCompileException e) {
			throw new IllegalStateException(e);
		}
	}

	// public String getActorClassName() {
	// return actorClassName;
	// }
	//
	// public String getSyntheticClassName() {
	// return syntheticClassName;
	// }
	//
	// public byte[] getBytecode() {
	// return bytecode;
	// }
	//
	// public Creator<?> getSyntheticCreatorInstance() {
	// return creator;
	// }
}
