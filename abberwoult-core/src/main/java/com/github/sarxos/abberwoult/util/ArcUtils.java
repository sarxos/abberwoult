package com.github.sarxos.abberwoult.util;

import static com.github.sarxos.abberwoult.util.ArcUtils.getQualifiers;
import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Qualifier;

import io.quarkus.arc.Arc;
import io.quarkus.arc.ArcContainer;

public class ArcUtils {

	private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];
	
	private static ArcContainer arc() {
		return Arc.container();
	}
	
	public static boolean isQualifier(Annotation annotation) {
		return annotation
			.annotationType()
			.isAnnotationPresent(Qualifier.class);
	}

	/**
	 * Get qualifiers from a {@link Parameter}.
	 *
	 * @param parameter
	 * @return
	 */
	public static Annotation[] getQualifiers(final Parameter parameter) {
		return filterQualifiers(parameter.getAnnotations());
	}

	/**
	 * Get qualifiers from a {@link Field}.
	 *
	 * @param field
	 * @return
	 */
	public static Annotation[] getQualifiers(final Field field) {
		return filterQualifiers(field.getAnnotations());
	}

	private static Annotation[] filterQualifiers(final Annotation[] annotations) {

		if (annotations.length == 0) {
			return EMPTY_ANNOTATION_ARRAY;
		}

		return Arrays
			.stream(annotations)
			.filter(ArcUtils::isQualifier)
			.collect(toListWithSameSizeAs(annotations))
			.toArray(EMPTY_ANNOTATION_ARRAY);
	}
	
	public static Object findBeanFor(final Parameter parameter) {
		final Annotation[] qualifiers = getQualifiers(parameter);
		final Type type = parameter.getParameterizedType();
		return findBeanFor(type, qualifiers);
	}

	public static Object findBeanFor(final Field field) {
		final Annotation[] qualifiers = getQualifiers(field);
		final Type type = field.getGenericType();
		return findBeanFor(type, qualifiers);
	}

	public static Object findBeanFor(final Type type, final Annotation[] qualifiers) {
		return arc()
			.instance((Class<?>) type, qualifiers)
			.get();
	}
}
