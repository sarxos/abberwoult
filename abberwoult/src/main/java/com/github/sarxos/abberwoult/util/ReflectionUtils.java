package com.github.sarxos.abberwoult.util;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Collections.emptyList;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;

import com.github.sarxos.abberwoult.exception.PreStartInvocationException;

import io.vavr.control.Option;


/**
 * Some reflection utilities.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ReflectionUtils {

	/**
	 * A {@link Class} mapping for primitive type names.
	 */
	@SuppressWarnings("serial")
	public final static Map<String, Class<?>> PRIMITIVES = new HashMap<String, Class<?>>() {
		{
			put("void", void.class);
			put("boolean", boolean.class);
			put("byte", byte.class);
			put("short", short.class);
			put("char", char.class);
			put("int", int.class);
			put("long", long.class);
			put("float", float.class);
			put("double", double.class);
		}
	};

	private static final Predicate<Parameter> HAS_VALID_ANNOTATION = p -> p.isAnnotationPresent(Valid.class);

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
	 * @param clazz the class to get methods from
	 * @param annotation the annotation which should be present on method
	 * @return List of methods annotated with a given annotation or empty list if not found
	 */
	public static Collection<Method> getAnnotatedMethodsFromClass(Class<?> clazz, final Class<? extends Annotation> annotation, final Class<?> stop) {

		final Collection<Method> methods = new ArrayDeque<>();

		while (clazz != stop && clazz != null) {
			for (final Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(annotation)) {
					methods.add(method);
				}
			}
			for (final Class<?> interf : clazz.getInterfaces()) {
				methods.addAll(getAnnotatedMethodsFromClass(interf, annotation, stop));
			}
			clazz = clazz.getSuperclass();
		}

		if (methods.isEmpty()) {
			return emptyList();
		}

		return methods;
	}

	public static Option<Method> getAnnotatedMethodFromClass(Class<?> clazz, final Class<? extends Annotation> annotation, final Class<?> stop) {

		while (clazz != stop && clazz != null) {
			for (final Method method : clazz.getDeclaredMethods()) {
				if (method.isAnnotationPresent(annotation)) {
					return Option.of(method);
				}
			}
			clazz = clazz.getSuperclass();
		}

		return Option.none();
	}

	/**
	 * Iterate through all {@link Method} from the class, all implemented interfaces and extended
	 * super classes to collect only these methods which defines a {@link Parameter} annotated with
	 * a given annotation. The resultant list is returned to the caller. It will be empty if no
	 * methods within a class, implemented interfaces or any of the super classes define a parameter
	 * with a given annotation present. This method will never return null.
	 *
	 * @param clazz the class to get methods from
	 * @param annotation the annotation which should be present on parameter
	 * @return List of methods containing parameter annotated with a given annotation or empty
	 */
	public static Collection<Method> getAnnotatedParameterMethodsFromClass(Class<?> clazz, final Class<? extends Annotation> annotation, final Class<?> stop) {

		final Collection<Method> methods = new ArrayDeque<>();

		while (clazz != stop && clazz != null) {
			for (final Method method : clazz.getDeclaredMethods()) {
				for (final Parameter parameter : method.getParameters()) {
					if (parameter.isAnnotationPresent(annotation)) {
						methods.add(method);
					}
				}
			}
			for (final Class<?> interf : clazz.getInterfaces()) {
				methods.addAll(getAnnotatedParameterMethodsFromClass(interf, annotation, stop));
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

	public static boolean isAbstract(Class<?> clazz) {
		return Modifier.isAbstract(clazz.getModifiers());
	}

	public static Class<?> getClazz(final String clazzName) {
		if (PRIMITIVES.containsKey(clazzName)) {
			return PRIMITIVES.get(clazzName);
		}
		try {
			return Class.forName(clazzName);
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	public static int getObjectDistance(Class<?> clazz) {
		for (int i = 0; true; i++) {
			if ((clazz = clazz.getSuperclass()) == null) {
				return i;
			}
		}
	}

	public static boolean hasValidableParameters(final Method method) {
		return Arrays
			.stream(method.getParameters())
			.filter(HAS_VALID_ANNOTATION)
			.findAny()
			.isPresent();
	}

	public static MethodHandle unreflect(final Class<?> clazz, final Method method) {
		try {
			return lookup().in(clazz).unreflect(accessible(method));
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void invokeVoidNoArg(final MethodHandle handle, final Object thiz) {
		try {
			handle.invoke(thiz);
		} catch (Throwable e) {
			throw new PreStartInvocationException(thiz, handle, e);
		}
	}

	public static boolean hasSameSignature(final Method a, final Method b) {
		return true
			&& a.getReturnType() == b.getReturnType()
			&& StringUtils.equals(a.getName(), b.getName())
			&& areParametersTheSame(a.getParameters(), b.getParameters());
	}

	private static boolean areParametersTheSame(final Parameter[] a, final Parameter[] b) {
		if (a.length != b.length) {
			return false;
		}
		for (int i = 0; i < a.length; i++) {
			if (a[i].getType() != b[i].getType()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Remove overridden methods from a given list.
	 *
	 * @param methods the methods to filter
	 */
	public static final List<Method> removeOverriddenFrom(final List<Method> methods) {

		final ArrayDeque<Method> input = new ArrayDeque<>(methods);

		methods.clear();

		while (input.size() > 0) {
			final Method method = input.poll();
			input.removeIf(m -> hasSameSignature(method, m));
			methods.add(method);
		}

		return methods;
	}

	/**
	 * Get position of first parameter in method annotated with a given annotation.
	 *
	 * @param method the method to get parameters from
	 * @param annotation the annotation to search for on parameter
	 * @return Parameter position or zero
	 */
	public static int getAnnotatedParameterPosition(final Method method, final Class<? extends Annotation> annotation) {

		final Parameter[] parameters = method.getParameters();

		if (parameters.length == 0) {
			throw new IllegalArgumentException("Method " + method + " has no parameters");
		}

		for (int i = 0; i < parameters.length; i++) {
			if (parameters[i].isAnnotationPresent(annotation)) {
				return i;
			}
		}

		return 0;
	}

	public static boolean isAnonymousOrAbstract(final Class<?> clazz) {
		return clazz.isAnonymousClass() || Modifier.isAbstract(clazz.getModifiers());
	}
}
