package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Qualifier;

import com.github.sarxos.abberwoult.annotation.Labeled;
import com.github.sarxos.abberwoult.exception.BeanInjectionException;

import io.vavr.control.Option;


public class BeanUtils {

	public static final Annotation[] EMPTY_ANNOTATION_ARRAY = new Annotation[0];

	private BeanUtils() {
		// utilities class
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
		return filterQualifiers(getAnnotations(parameter));
	}

	public static Annotation[] getAnnotations(final Parameter parameter) {
		return parameter.getAnnotations();
	}

	/**
	 * Get qualifiers from a {@link Field}.
	 *
	 * @param field
	 * @return
	 */
	public static Annotation[] getQualifiers(final Field field) {
		return filterQualifiers(getAnnotations(field));
	}

	public static Annotation[] getAnnotations(final Field field) {
		return field.getAnnotations();
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

	/**
	 * Get specific qualifier (an annotation annotated with {@link Qualifier}) from an
	 * {@link InjectionPoint}.
	 *
	 * @param injection the {@link InjectionPoint} to get qualifier from
	 * @param qualifier
	 * @return
	 */
	public static <T extends Annotation> Option<T> getQualifier(final InjectionPoint injection, final Class<T> qualifier) {

		final Optional<T> q = injection
			.getQualifiers()
			.stream()
			.filter(qualifier::isInstance)
			.map(qualifier::cast)
			.findAny();

		return Option.ofOptional(q);
	}

	/**
	 * Get topic name from {@link Labeled} annotation.
	 *
	 * @param injection the {@link InjectionPoint}
	 * @return Topic name
	 */
	public static String getLabel(final InjectionPoint injection) {

		if (injection == null) {
			throw new NullPointerException("Injection point is null");
		}

		return getQualifier(injection, Labeled.class)
			.map(Labeled::value)
			.getOrElseThrow(() -> new NoLabelException(injection));
	}

	@SuppressWarnings("serial")
	public static class NoLabelException extends BeanInjectionException {
		public NoLabelException(final InjectionPoint injection) {
			super("No " + Labeled.class + " annotation found on " + injection.getMember());
		}
	}
}
