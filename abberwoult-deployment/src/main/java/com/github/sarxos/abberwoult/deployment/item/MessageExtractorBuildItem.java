package com.github.sarxos.abberwoult.deployment.item;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;

import com.github.sarxos.abberwoult.deployment.util.MessageExtractorCodeGenerator;

import akka.cluster.sharding.ShardRegion.MessageExtractor;
import io.quarkus.builder.item.MultiBuildItem;
import io.vavr.control.Option;
import javassist.CannotCompileException;
import javassist.CtClass;


public final class MessageExtractorBuildItem extends MultiBuildItem {

	private final MessageExtractorCodeGenerator generator = new MessageExtractorCodeGenerator();
	private final ClassInfo messageClass;
	private final CtClass syntheticClass;
	private final MessageExtractor extractor;
	private final byte[] bytecode;

	public MessageExtractorBuildItem(ClassInfo messageClass) {
		this.messageClass = messageClass;
		this.syntheticClass = generator.generate(messageClass);
		this.extractor = createExtractor(syntheticClass);
		this.bytecode = toBytecode(syntheticClass);
	}

	private MessageExtractor createExtractor(final CtClass cc) {
		return Option.of(cc).toTry()
			.mapTry(CtClass::toClass)
			.mapTry(Class::newInstance)
			.map(MessageExtractor.class::cast)
			.get();
	}

	private byte[] toBytecode(CtClass cc) {
		try {
			return cc.toBytecode();
		} catch (IOException | CannotCompileException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getMessageClassName() {
		return messageClass.name().toString();
	}

	public CtClass getSyntheticClass() {
		return syntheticClass;
	}

	public String getSyntheticClassName() {
		return syntheticClass.getName();
	}

	public byte[] getBytecode() {
		return bytecode;
	}

	public MessageExtractor getSyntheticExtractorInstance() {
		return extractor;
	}
}
