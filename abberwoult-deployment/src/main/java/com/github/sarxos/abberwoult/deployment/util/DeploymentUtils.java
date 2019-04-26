package com.github.sarxos.abberwoult.deployment.util;

import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.ASSISTED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.INJECT_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.MESSAGE_HANDLER_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.SIMPLE_ACTOR_CLASS;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.VALID_ANNOTATION;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toCollection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget.Kind;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.quarkus.arc.processor.AnnotationsTransformer.TransformationContext;


public class DeploymentUtils {

	private DeploymentUtils() {
	}

	public static boolean isMethodAnnotation(final AnnotationInstance instance) {
		return instance.target().kind() == Kind.METHOD;
	}

	public static boolean isMethodParameterAnnotation(final AnnotationInstance instance) {
		return instance.target().kind() == Kind.METHOD_PARAMETER;
	}

	public static boolean isValidAnnotation(final AnnotationInstance instance) {
		return instance.name().equals(VALID_ANNOTATION);
	}

	public static boolean isInjectAnnotation(final AnnotationInstance instance) {
		return instance.name().equals(INJECT_ANNOTATION);
	}

	public static boolean isAssistedAnnotation(final AnnotationInstance instance) {
		return instance.name().equals(ASSISTED_ANNOTATION);
	}

	public static short toMethodParameterPosition(final AnnotationInstance instance) {
		return instance
			.target()
			.asMethodParameter()
			.position();
	}

	public static ClassInfo methodDeclaringClass(AnnotationInstance instance) {
		return instance.target().asMethod().declaringClass();
	}

	/**
	 * Predicate to check if method is not already present in a set.
	 *
	 * @param methods a set of methods to compare
	 * @return New predicate.
	 */
	public static Predicate<MethodInfo> notAlreadypresentIn(final Set<MethodInfo> methods) {
		return a -> {
			for (final MethodInfo b : methods) {
				if (isSameMethod(a, b)) {
					return false; // same method means it was overridden
				}
			}
			return true;
		};
	}

	/**
	 * Return true if method a has the same signature as method b.
	 *
	 * @param a the method a
	 * @param b the method b
	 * @return True if a and b has the same signature.
	 */
	public static boolean isSameMethod(final MethodInfo a, final MethodInfo b) {

		final String nameA = a.name();
		final String nameB = b.name();

		// methods to be the same must have the same name

		if (!StringUtils.equals(nameA, nameB)) {
			return false;
		}

		final List<Type> parametersA = a.parameters();
		final List<Type> parametersB = a.parameters();

		// methods to be the same must have the same number of arguments

		if (parametersA.size() != parametersB.size()) {
			return false;
		}

		final int length = parametersA.size();

		// methods to be the same must have the same argument types

		for (int i = 0; i < length; i++) {

			final Type typeA = parametersA.get(i);
			final Type typeB = parametersB.get(i);

			if (!Objects.equals(typeA, typeB)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check if given method is a message handler.
	 *
	 * @param method a method to check
	 * @return True if given method is a message handler, false otherwise
	 */
	private static boolean isMessageHandler(final MethodInfo method) {
		return method.hasAnnotation(MESSAGE_HANDLER_ANNOTATION);
	}

	public static boolean isMessageHandler(final TransformationContext tc) {

		// only methods can be message handlers

		if (!tc.isMethod()) {
			return false;
		}

		// check if method is annotated with the proper annotation

		final MethodInfo info = tc.getTarget().asMethod();
		final AnnotationInstance annotation = info.annotation(MESSAGE_HANDLER_ANNOTATION);

		// null annotation means that given annotation was not present on the element

		if (annotation == null) {
			return false;
		}

		// if we found given annotation on the element then we need to double check if
		// this is method annotations and not for example parameter or type annotation,
		// this can be done by checking target kind which needs to indicate a method

		return annotation.target().kind() == Kind.METHOD;
	}

	private static Set<MethodInfo> findMessageHandlers(final DotName dn, final IndexView index) {
		if (dn == null || dn.equals(SIMPLE_ACTOR_CLASS)) {
			return emptySet();
		} else {
			return findMessageHandlers(index.getClassByName(dn), index);
		}
	}

	/**
	 * Recursively find message handlers by scanning class, all implemented interfaces and all
	 * superclasses.
	 *
	 * @param clazz the class to scan for message handlers
	 * @param index the jandex index
	 * @return A set of message handlers found
	 */
	public static Set<MethodInfo> findMessageHandlers(final ClassInfo clazz, final IndexView index) {

		if (clazz == null) {
			return emptySet();
		}

		final Set<MethodInfo> handlers = clazz.methods()
			.stream()
			.filter(DeploymentUtils::isMessageHandler)
			.collect(toCollection(LinkedHashSet::new));

		final Consumer<DotName> collect = dn -> findMessageHandlers(dn, index)
			.stream()
			.filter(notAlreadypresentIn(handlers))
			.forEach(handlers::add);

		clazz
			.interfaceNames()
			.forEach(collect);

		collect.accept(clazz.superName());

		return handlers;
	}
}
