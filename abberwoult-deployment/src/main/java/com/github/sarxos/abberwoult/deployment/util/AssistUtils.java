package com.github.sarxos.abberwoult.deployment.util;

import org.jboss.jandex.DotName;

import io.vavr.control.Option;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;


public class AssistUtils {

	private static final String RUNTIME_ANNOTATIONS = AnnotationsAttribute.visibleTag;

	public static void addAnnotation(final CtMethod method, final DotName clazz) {
		addAnnotation(method, clazz.toString());
	}

	public static void addAnnotation(final CtMethod method, final String clazz) {

		final MethodInfo info = method.getMethodInfo();
		final ConstPool constpool = info.getConstPool();

		final AnnotationsAttribute attribute = Option
			.of(info.getAttribute(RUNTIME_ANNOTATIONS))
			.map(AnnotationsAttribute.class::cast)
			.getOrElse(() -> new AnnotationsAttribute(constpool, RUNTIME_ANNOTATIONS));

		final Annotation annotation = new Annotation(clazz, constpool);

		attribute.addAnnotation(annotation);
		info.addAttribute(attribute);

		method
			.getMethodInfo()
			.addAttribute(attribute);
	}
}
