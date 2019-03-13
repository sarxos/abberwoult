package com.github.sarxos.abberwoult.util;

import static java.util.Collections.emptyList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Some reflection utilities.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ReflectionUtils {

	/**
	 * Make sure {@link Field} is accessible.
	 *
	 * @param field the {@link Field}
	 * @return The same {@link Field}, but accessible
	 */
	public static Field accessible(Field field) {
		return (Field) accessible0(field);
	}

	/**
	 * Make sure {@link Method} is accessible.
	 *
	 * @param method the {@link Method}
	 * @return The same {@link Method}, but accessible
	 */
	public static Method accessible(Method method) {
		return (Method) accessible0(method);
	}

	/**
	 * Make sure {@link Constructor} is accessible.
	 *
	 * @param constructor the {@link Constructor}
	 * @return The same {@link Constructor}, but accessible
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> accessible(Constructor<T> constructor) {
		return (Constructor<T>) accessible0(constructor);
	}

	/**
	 * Make sure {@link AccessibleObject} is accessible.
	 *
	 * @param object the {@link AccessibleObject}
	 * @return The same {@link AccessibleObject}, but accessible
	 */
	private static AccessibleObject accessible0(AccessibleObject object) {
		if (!object.isAccessible()) {
			object.setAccessible(true);
		}
		return object;
	}

	/**
	 * Instantiate given class by invoking default constructor.
	 *
	 * @param clazz the class to be instantiated
	 * @return The instance of a given class
	 */
	public static <T> T instantiate(Class<T> clazz) {

		Constructor<T> constructor;
		try {
			constructor = clazz.getDeclaredConstructor();
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(e);
		}
		if (constructor == null) {
			throw new IllegalStateException("The " + clazz + " requires default (no-arg) constructor");
		}

		try {
			return accessible(constructor).newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public static <T extends Annotation> T getAnnotationFromClass(Class<?> clazz, final Class<T> annotation) {

		T instance = null;
		do {

			instance = clazz.getAnnotation(annotation);
			if (instance != null) {
				return instance;
			}

			for (Class<?> iclazz : clazz.getInterfaces()) {
				instance = getAnnotationFromClass(iclazz, annotation);
				if (instance != null) {
					return instance;
				}
			}

			clazz = clazz.getSuperclass();

		} while (clazz != null);

		return null;
	}

	/**
	 * Iterate through all fields from the class and all its superclasses to collect fields
	 * annotated with a given annotation into a list. The resultant list is returned. It will be
	 * empty if no field within class or any of the superclasses is annotated with the given
	 * annotation.
	 *
	 * @param clazz the class to get fields from
	 * @param annotation the annotation which should be present on field
	 * @return List of fields annotated with a given annotation or empty list if not found
	 */
	public static List<Field> getAnnotatedFieldsFromClass(Class<?> clazz, final Class<? extends Annotation> annotation) {

		final List<Field> fields = new ArrayList<>();

		while (clazz != null) {
			for (final Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(annotation)) {
					fields.add(field);
				}
			}
			clazz = clazz.getSuperclass();
		}

		if (fields.isEmpty()) {
			return emptyList();
		}

		return fields;
	}

	/**
	 * Iterate through all methods from the class and all its superclasses to collect only these
	 * methods which are annotated with a given annotation. The resultant list is returned to the
	 * caller. It will be empty if no methods within a class or any of the superclasses is annotated
	 * with a provided annotation.
	 *
	 * @param clazz the class to get methdos from
	 * @param annotation the annotation which should be present on field
	 * @return List of methods annotated with a given annotation or empty list if not found
	 */
	public static List<Method> getAnnotatedMethodsFromClass(Class<?> clazz, final Class<? extends Annotation> annotation) {

		final List<Method> methods = new ArrayList<>();

		while (clazz != null) {
			for (final Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(annotation)) {
					methods.add(method);
				}
			}
			clazz = clazz.getSuperclass();
		}

		if (methods.isEmpty()) {
			return emptyList();
		}

		return methods;
	}

	public static Object invoke(final Object thiz, final Method method) {
		try {
			return accessible(method).invoke(thiz);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void set(final Object thiz, final Field field, final Object value) {
		try {
			accessible(field).set(thiz, value);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new IllegalStateException(e);
		}
	}

	public static Object get(final Object thiz, final Field field) {
		try {
			return accessible(field).get(thiz);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new IllegalStateException(e);
		}
	}
}
