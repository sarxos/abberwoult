package com.github.sarxos.abberwoult.deployment.util;

import static com.github.sarxos.abberwoult.DotNames.SHARD_ENTITY_ID_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.SHARD_ID_ANNOTATION;
import static org.apache.commons.lang3.CharUtils.isAsciiAlphaUpper;
import static org.apache.commons.lang3.StringUtils.capitalize;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

import com.github.sarxos.abberwoult.ShardMessageExtractor.FieldReader;
import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;
import com.github.sarxos.abberwoult.jandex.Reflector.FieldRef;
import com.github.sarxos.abberwoult.jandex.Reflector.MethodRef;

import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.NotFoundException;


public class FieldReaderGenerator {

	private static final String FIELD_READER_CLASS_NAME = FieldReader.class.getName();
	private static final ClassPool POOL = getClassPool();
	private static final CtClass FIELD_READER_CT_CLASS = getFieldReaderCtClass();
	private static final String PREFIX_GET = "get";
	private static final String PREFIX_IS = "is";

	/**
	 * Create new method from a source code.
	 *
	 * @param cc the declaring {@link CtClass}
	 * @param code the source code
	 * @param args the printf arguments used in the code
	 * @return New {@link CtMethod}
	 */
	private CtMethod method(final CtClass cc, final String code, final Object... args) {
		final String source = String.format(code, args);
		try {
			return CtNewMethod.make(source, cc);
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Generate class which implements {@link FieldReader}.
	 *
	 * @param info the {@link ClassInfo} linked to the message {@link Class}
	 * @return New {@link CtClass}
	 */
	public CtClass generate(final ClassRef info) {
		try {
			return generate0(info);
		} catch (CannotCompileException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * @param clazz the message class name
	 * @return New {@link CtClass}
	 * @throws CannotCompileException
	 */
	private CtClass generate0(final ClassRef clazz) throws CannotCompileException {

		final String clazzName = clazz.getName();
		final String shardIdGetterName = getAnnotatedMethodName(clazz, SHARD_ID_ANNOTATION);
		final String shardEntityIdGetterName = getAnnotatedMethodName(clazz, SHARD_ENTITY_ID_ANNOTATION);
		final String syntheticClassName = clazzName + "_FieldReader";

		try {
			return POOL.get(syntheticClassName);
		} catch (NotFoundException e) {

			final CtClass cc = POOL.makeClass(syntheticClassName);
			cc.setInterfaces(new CtClass[] { FIELD_READER_CT_CLASS });
			cc.setModifiers(Modifier.FINAL | Modifier.PUBLIC);
			cc.addMethod(method(cc, "public Object readShardId(Object m) { return value(((%s) m).%s()); }", clazzName, shardIdGetterName));
			cc.addMethod(method(cc, "public Object readShardEntityId(Object m) { return value(((%s) m).%s()); }", clazzName, shardEntityIdGetterName));
			cc.debugWriteFile("target/abberwoult/generated-classes");

			return cc;
		}
	}

	private String getAnnotatedMethodName(final ClassRef clazz, final DotName annotation) {

		final Option<String> method = Stream
			.ofAll(clazz.getMethods())
			.find(m -> m.hasAnnotation(annotation))
			.map(MethodRef::getName);

		if (method.isDefined()) {
			return method.get();
		}

		final FieldRef field = Stream
			.ofAll(clazz.getFields())
			.find(f -> f.hasAnnotation(annotation))
			.getOrElseThrow(() -> new NoAnnotationFoundException(annotation, clazz));

		return getReadMethod(clazz, field);
	}

	private String getGetterName(final String name, final boolean isBoolean) {
		final String prefix = isBoolean ? PREFIX_IS : PREFIX_GET;
		if (isBoolean && name.startsWith(PREFIX_IS) && name.length() > 2 && isAsciiAlphaUpper(name.charAt(2))) {
			return name;
		} else {
			return prefix + capitalize(name);
		}
	}

	private String getReadMethod(final ClassRef clazz, FieldRef field) {

		final String fieldTypeName = field.getTypeName();
		final boolean isBoolean = StringUtils.equals(fieldTypeName, "boolean");
		final String getter = getGetterName(field.getName(), isBoolean);
		final MethodRef method = clazz.getMethodByName(getter);

		if (method == null) {
			throw new IllegalStateException("No getter " + getter + " found for " + field + " in " + clazz);
		}

		final String methodReturnType = method.getReturnTypeName();
		final boolean isVoid = StringUtils.equals(methodReturnType, "void");

		if (isVoid) {
			throw new IllegalStateException("Void method " + method + " cannot be a getter for " + field + " in " + clazz);
		}
		if (method.getParametersCount() == 0) {
			return method.getName();
		}

		throw new IllegalStateException("Method " + method + " must have no arguments to be a getter for " + field + " in " + clazz);
	}

	private static final ClassPool getClassPool() {
		final ClassPool cp = ClassPool.getDefault();
		cp.insertClassPath(new CurrentThreadContextClassPath());
		return cp;
	}

	private static final CtClass getFieldReaderCtClass() {
		return Try
			.of(() -> POOL.get(FIELD_READER_CLASS_NAME))
			.get();
	}
}

@SuppressWarnings("serial")
class NoAnnotationFoundException extends IllegalStateException {
	public NoAnnotationFoundException(DotName annotation, ClassRef clazz) {
		super("Neither method nor field is annotated with " + annotation + " in " + clazz);
	}
}
