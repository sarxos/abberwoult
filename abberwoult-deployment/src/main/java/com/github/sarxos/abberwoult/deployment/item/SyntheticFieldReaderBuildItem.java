package com.github.sarxos.abberwoult.deployment.item;

import java.io.IOException;

import org.jboss.jandex.ClassInfo;

import com.github.sarxos.abberwoult.ShardMessageExtractor.FieldReader;
import com.github.sarxos.abberwoult.deployment.util.FieldReaderGenerator;

import io.quarkus.builder.item.MultiBuildItem;
import io.vavr.control.Option;
import javassist.CannotCompileException;
import javassist.CtClass;


/**
 * A {@link MultiBuildItem} designed to transfer information about the message class and generate
 * bytecode for a synthetic {@link FieldReader} implementation.
 *
 * @author Bartosz Firyn (sarxos)
 */
public final class SyntheticFieldReaderBuildItem extends MultiBuildItem {

	private static final FieldReaderGenerator GENERATOR = new FieldReaderGenerator();

	private final String messageClassName;
	private final String syntheticClassName;
	private final FieldReader reader;
	private final byte[] bytecode;

	public SyntheticFieldReaderBuildItem(final ClassInfo messageClass) {

		final CtClass cc = GENERATOR.generate(messageClass);

		this.messageClassName = messageClass.name().toString();
		this.syntheticClassName = cc.getName();
		this.reader = instantiate(cc);
		this.bytecode = toBytecode(cc);
	}

	/**
	 * Instantiate {@link CtClass} as {@link FieldReader} implementation.
	 *
	 * @param cc the generated {@link CtClass} which implements {@link FieldReader}
	 * @return New {@link FieldReader} instance
	 */
	private static FieldReader instantiate(final CtClass cc) {
		return Option.of(cc).toTry()
			.mapTry(CtClass::toClass)
			.mapTry(Class::newInstance)
			.map(FieldReader.class::cast)
			.get();
	}

	/**
	 * Converts generated {@link CtClass} into bytecode.
	 *
	 * @param cc the generated {@link CtClass}
	 * @return Bytecode
	 */
	private static byte[] toBytecode(final CtClass cc) {
		try {
			return cc.toBytecode();
		} catch (IOException | CannotCompileException e) {
			throw new IllegalStateException(e);
		}
	}

	public String getMessageClassName() {
		return messageClassName;
	}

	public String getSyntheticClassName() {
		return syntheticClassName;
	}

	public byte[] getBytecode() {
		return bytecode;
	}

	public FieldReader getSyntheticFieldReaderInstance() {
		return reader;
	}
}
