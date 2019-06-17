package com.github.sarxos.abberwoult.deployment.item;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;

import com.github.sarxos.abberwoult.ShardMessageExtractor.FieldReader;
import com.github.sarxos.abberwoult.deployment.util.FieldReaderGenerator;

import io.quarkus.builder.item.MultiBuildItem;
import io.vavr.control.Option;
import javassist.CannotCompileException;
import javassist.CtClass;


public final class FieldReaderBuildItem extends MultiBuildItem {

	private final FieldReaderGenerator generator = new FieldReaderGenerator();
	private final ClassInfo messageClass;
	private final CtClass syntheticClass;
	private final FieldReader reader;
	private final byte[] bytecode;

	public FieldReaderBuildItem(ClassInfo messageClass) {
		this.messageClass = messageClass;
		this.syntheticClass = generator.generate(messageClass);
		this.reader = createReader(syntheticClass);
		this.bytecode = toBytecode(syntheticClass);
	}

	private FieldReader createReader(final CtClass cc) {
		return Option.of(cc).toTry()
			.mapTry(CtClass::toClass)
			.mapTry(Class::newInstance)
			.map(FieldReader.class::cast)
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

	public FieldReader getSyntheticFieldReaderInstance() {
		return reader;
	}
}
