package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;

import javax.inject.Qualifier;


public class BeanUtils {

	private static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

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
			.filter(BeanUtils::isQualifier)
			.collect(toListWithSameSizeAs(annotations))
			.toArray(EMPTY_ANNOTATION_ARRAY);
	}

}
