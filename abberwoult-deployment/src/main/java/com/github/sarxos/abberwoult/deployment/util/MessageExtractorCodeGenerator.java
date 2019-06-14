package com.github.sarxos.abberwoult.deployment.util;

import static com.github.sarxos.abberwoult.DotNames.SHARD_ENTITY_ID_ANNOTATION;
import static com.github.sarxos.abberwoult.DotNames.SHARD_ID_ANNOTATION;
import static org.apache.commons.lang3.CharUtils.isAsciiAlphaUpper;
import static org.apache.commons.lang3.StringUtils.capitalize;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;

import akka.cluster.sharding.ShardRegion.MessageExtractor;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;


public class MessageExtractorCodeGenerator {

	private static final Class<MessageExtractor> EXTRACTOR_CLASS = MessageExtractor.class;
	private static final String EXTRACTOR_CLASS_NAME = MessageExtractor.class.getName();
	private static final ClassPool CLASS_POOL = getClassPool();
	private static final CtClass MESSAGE_EXTRACTOR_CT_CLASS = getMessageExtractorCtClass();
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
	private CtMethod method(CtClass cc, String code, Object... args) {
		final String source = String.format(code, args);
		try {
			return CtNewMethod.make(source, cc);
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Generate class which implements {@link MessageExtractor} and consume specific
	 *
	 * @param info the {@link ClassInfo} linked to the message {@link Class}
	 * @return New {@link CtClass}
	 */
	public CtClass generate(final ClassInfo info) {
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
	private CtClass generate0(final ClassInfo clazz) throws CannotCompileException {

		final String clazzName = clazz.name().toString();
		final String shardIdGetterName = getAnnotatedMethodName(clazz, SHARD_ID_ANNOTATION);
		final String shardEntityIdGetterName = getAnnotatedMethodName(clazz, SHARD_ENTITY_ID_ANNOTATION);

		final CtClass cc = CLASS_POOL.makeClass(clazzName + "_MessageExtractor");
		cc.setInterfaces(new CtClass[] { MESSAGE_EXTRACTOR_CT_CLASS });
		cc.addMethod(method(cc, "public String shardId(Object m) { return String.valueOf(((%s) m).%s()); }", clazzName, shardIdGetterName));
		cc.addMethod(method(cc, "public String entityId(Object m) { return String.valueOf(((%s) m).%s()); }", clazzName, shardEntityIdGetterName));
		cc.addMethod(method(cc, "public Object entityMessage(Object m) { return m; }"));
		cc.debugWriteFile("target/message-extractors");

		return cc;
	}

	private String getAnnotatedMethodName(final ClassInfo clazz, final DotName annotation) {

		final Option<String> method = Stream
			.ofAll(clazz.methods())
			.find(m -> m.hasAnnotation(annotation))
			.map(MethodInfo::name);

		if (method.isDefined()) {
			return method.get();
		}

		final FieldInfo field = Stream
			.ofAll(clazz.fields())
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

	private String getReadMethod(final ClassInfo clazz, FieldInfo field) {

		final String fieldTypeName = field.type().name().toString();
		final boolean isBoolean = StringUtils.equals(fieldTypeName, "boolean");
		final String getter = getGetterName(field.name(), isBoolean);
		final MethodInfo method = clazz.method(getter);

		if (method == null) {
			throw new IllegalStateException("No getter " + getter + " found for " + field + " in " + clazz);
		}

		final String methodReturnType = method.returnType().name().toString();
		final boolean isVoid = StringUtils.equals(methodReturnType, "void");

		if (isVoid) {
			throw new IllegalStateException("Void method " + method + " cannot be a getter for " + field + " in " + clazz);
		}
		if (method.parameters().isEmpty()) {
			return method.name();
		}

		throw new IllegalStateException("Method " + method + " must have no arguments to be a getter for " + field + " in " + clazz);
	}

	private static final ClassPool getClassPool() {
		final ClassPool cp = ClassPool.getDefault();
		cp.insertClassPath(new ClassClassPath(EXTRACTOR_CLASS));
		return cp;
	}

	private static final CtClass getMessageExtractorCtClass() {
		return Try
			.of(() -> CLASS_POOL.get(EXTRACTOR_CLASS_NAME))
			.get();
	}
}

@SuppressWarnings("serial")
class NoAnnotationFoundException extends IllegalStateException {
	public NoAnnotationFoundException(DotName annotation, ClassInfo clazz) {
		super("Neither method nor field is annotated with " + annotation + " in " + clazz);
	}
}
