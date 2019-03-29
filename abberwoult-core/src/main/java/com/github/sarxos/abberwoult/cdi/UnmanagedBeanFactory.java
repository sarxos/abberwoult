package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.accessible;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.invoke;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.isAbstract;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.set;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.reflect.TypeUtils.isAssignable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.inject.Inject;

import com.github.sarxos.abberwoult.annotation.Assisted;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import io.vavr.control.Option;


/**
 * Factory for instances which are <b>not</b> managed by CDI. These instances, however, are wired in
 * accordance to the same principles as the managed CDI beans.
 *
 * @author Bartosz Firyn (sarxos)
 * @param <T>
 */
public class UnmanagedBeanFactory<T> {

	/**
	 * An empty objects array.
	 */
	private static final Object[] EMPTY_OBJECTS_ARRAY = new Object[0];

	/**
	 * Primitive type to boxed type mapping.
	 */
	private static final Map<Type, Type> BOXED = new HashMap<>();
	static {
		BOXED.put(char.class, Character.class);
		BOXED.put(boolean.class, Boolean.class);
		BOXED.put(byte.class, Byte.class);
		BOXED.put(short.class, Short.class);
		BOXED.put(int.class, Integer.class);
		BOXED.put(long.class, Long.class);
		BOXED.put(float.class, Float.class);
		BOXED.put(double.class, Double.class);
		BOXED.put(void.class, Void.class);
	}

	/**
	 * {@link BeanLocator} used to perform dependency injection.
	 */
	private final BeanLocator locator;

	/**
	 * Constructee class.
	 */
	private final Class<T> clazz;

	/**
	 * Class in hierarchy at which injection will stop.
	 */
	private final Class<?> stop;

	/**
	 * Additional, but still optional, actor constructor arguments.
	 */
	private final Object[] args;

	/**
	 * Creates new {@link UnmanagedBeanFactory} instance with given {@link BeanLocator} and class
	 * which describes the actor to be created. This constructor takes also vararg objects list
	 * which are the arguments (optional) to be passed down to the class constructor. Please note
	 * that in case when class constructor is annotated with {@link Inject}, the arguments list will
	 * be ignored (creator will try to resolve all necessary arguments using dependency injection
	 * support and original arguments are ignored).
	 *
	 * @param locator the service locator to be used to wire created instance
	 * @param clazz the actor class which should be created
	 * @param stop the class in hierarchy at which injector should stop searching injection points
	 * @param args the constructor arguments
	 */
	public UnmanagedBeanFactory(final BeanLocator locator, final Class<T> clazz, final Class<?> stop, final Object... args) {
		this.locator = locator;
		this.clazz = clazz;
		this.stop = stop;
		this.args = args;
	}

	/**
	 * Return boxed type for primitive or, if there is no boxed type, a type itself.
	 *
	 * @param type the input type
	 * @return Boxed type if input is primitive type, or input type otherwise
	 */
	private static Type boxed(final Type type) {
		return BOXED.getOrDefault(type, type);
	}

	/**
	 * Find the most suitable constructor to be used.
	 *
	 * @param clazz the class to get constructors from
	 * @param args the arguments used by the actor constructor
	 * @return Best matching {@link Constructor}
	 * @throws NoSuitableConstructorFoundException if no suitable constructor has been found
	 */
	private static <T> Constructor<T> findMatchingConstructor(final Class<T> clazz, final Object[] args) {

		@SuppressWarnings("unchecked")
		final Constructor<T>[] constructors = (Constructor<T>[]) clazz.getDeclaredConstructors();

		// in case if there is only one constructor available in a given class, there is no
		// ambiguity - just use it

		if (constructors.length == 1) {
			return constructors[0];
		}

		// we need to find appropriate one in case there are more then one constructor, this can be
		// done by checking types of arguments array vs constructor types expected in the
		// constructor input

		ctors: for (final Constructor<T> constructor : constructors) {

			// ignore wired constructors

			if (constructor.isAnnotationPresent(Inject.class)) {
				continue;
			}

			// this is not the one we are looking for if size of constructor input array is
			// different than actor creator args

			final Type[] types = constructor.getParameterTypes();
			if (types.length != args.length) {
				continue;
			}

			// loop through the expected types and check if they match argument classes

			for (int i = 0; i < types.length; i++) {
				if (args[i] == null || isAssignable(boxed(args[i].getClass()), boxed(types[i]))) {
					continue;
				} else {
					continue ctors;
				}
			}

			// everything is fine, this is the constructor we are looking for

			return constructor;
		}

		// in case we haven't found matching constructor in previous steps we have to check if at
		// least one wired constructor is present in a given class and assume it's the one user was
		// looking for (this is true only in case if no arguments has been passed to the actor
		// creator since wired constructors does not use creator arguments, only injectees are used
		// to wire such constructor)

		// find wired constructors (there is no ambiguity check, just take the first one found)

		if (args.length == 0) {
			for (final Constructor<T> constructor : constructors) {
				if (constructor.isAnnotationPresent(Inject.class)) {
					return constructor;
				}
			}
		}

		// throw exception if no suitable constructor has been found

		throw new NoSuitableConstructorFoundException(clazz, constructors, args);
	}

	public T create() {

		// find constructors (we expect only one)

		final T instance;
		final Constructor<T> constructor = findMatchingConstructor(clazz, args);

		if (constructor.isAnnotationPresent(Inject.class)) {
			instance = instantiate(constructor, inject(constructor, args));
		} else {
			instance = instantiate(constructor, args);
		}

		inject(instance);
		postConstruct(instance);

		return instance;
	}

	private Object[] inject(final Constructor<T> constructor, final Object[] args) {

		final Parameter[] parameters = constructor.getParameters();
		final Deque<Object> arguments = new ArrayDeque<>(asList(args));

		final Object[] wired = Arrays
			.stream(parameters)
			.map(parameter -> findArgumentForParameter(parameter, arguments))
			.collect(toListWithSameSizeAs(parameters))
			.toArray(EMPTY_OBJECTS_ARRAY);

		// at this point we should pick up all of available arguments and remaining
		// list should be empty

		if (arguments.isEmpty()) {
			return wired;
		}

		final int required = constructor.getParameterCount();
		final int provided = wired.length + arguments.size();

		throw new WrongNumberOfArgumentsException(constructor, required, provided);
	}

	private void inject(final T instance) {
		inject(instance, instance.getClass());
	}

	/**
	 * Recursive injection process for class hierarchy (will stop injecting after reaching stop
	 * class or null).
	 *
	 * @param instance the instance to
	 * @param clazz
	 */
	private void inject(final T instance, final Class<?> clazz) {

		// stop injecting fields in hierarchy when we reached stop class or we reached
		// top (a class is null)

		if (clazz == stop || clazz == null) {
			return;
		}

		for (Field field : clazz.getDeclaredFields()) {
			if (field.isAnnotationPresent(Inject.class)) {
				inject(instance, field);
			} else {
				continue;
			}
		}

		inject(instance, clazz.getSuperclass());
	}

	private void inject(final T instance, final Field field) {
		final Object injectee = locator.findBeanFor(field);
		if (injectee != null) {
			set(instance, accessible(field), injectee);
		} else {
			throw new FieldInjecteeNotAvailableException(field);
		}
	}

	private Object findArgumentForParameter(final Parameter parameter, final Deque<Object> args) {
		if (parameter.isAnnotationPresent(Assisted.class)) {
			return args.remove();
		} else {
			try {
				return Option
					.of(locator.findBeanFor(parameter))
					.getOrElseThrow(() -> new ParameterInjecteeNotAvailableException(parameter));
			} catch (UnsatisfiedResolutionException e) {
				throw new UnsatisfiedParameterInjectionException(parameter, e);
			}
		}
	}

	private T instantiate(final Constructor<T> constructor, final Object[] args) {

		final int requiredArgsCount = constructor.getParameterCount();
		final int providedArgsCount = args.length;

		if (requiredArgsCount != providedArgsCount) {
			throw new WrongNumberOfArgumentsException(constructor, requiredArgsCount, providedArgsCount);
		}

		try {
			return accessible(constructor).newInstance(args);
		} catch (InstantiationException e) {
			if (isAbstract(clazz)) {
				throw new AbstractClassNotSupportedException(clazz, e);
			} else {
				throw new BeanInstantiationException(clazz, e);
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new BeanInstantiationException(clazz, e);
		}
	}

	private void postConstruct(T instance) {
		ReflectionUtils
			.getAnnotatedMethodFromClass(clazz, PostConstruct.class, stop)
			.map(ReflectionUtils::accessible)
			.forEach(method -> invoke(instance, method));
	}

	public BeanLocator getBeanManager() {
		return locator;
	}

	public Class<T> getActorClass() {
		return clazz;
	}

	public Class<?> getStopClass() {
		return stop;
	}

	public Object[] getArgs() {
		return args;
	}

	/**
	 * This exception is being thrown when {@link UnmanagedBeanFactory} cannot find suitable
	 * constructor to create instance with the arguments.
	 */
	@SuppressWarnings("serial")
	public static final class NoSuitableConstructorFoundException extends BeanInjectionException {

		private final Class<?> clazz;
		private final Collection<Constructor<?>> constructors;
		private final Collection<Object> arguments;

		/**
		 * @param clazz the class
		 * @param constructors the actor constructors
		 * @param arguments the arguments
		 */
		public NoSuitableConstructorFoundException(final Class<?> clazz, final Constructor<?>[] constructors, final Object[] arguments) {
			this.clazz = clazz;
			this.constructors = Arrays.asList(constructors);
			this.arguments = Arrays.stream(arguments)
				.map(Object::getClass)
				.collect(toListWithSameSizeAs(arguments));
		}

		@Override
		public String getMessage() {
			return ""
				+ "No suitable constructor found to create " + clazz + " instance, "
				+ "candidates are " + constructors + ", but arguments are " + arguments;
		}
	}

	@SuppressWarnings("serial")
	public static final class FieldInjecteeNotAvailableException extends BeanInjectionException {
		public FieldInjecteeNotAvailableException(final Field field) {
			super("Failed to resolve injectee for field " + field);
		}
	}

	@SuppressWarnings("serial")
	public static final class ParameterInjecteeNotAvailableException extends BeanInjectionException {
		public ParameterInjecteeNotAvailableException(final Parameter parameter) {
			super("Failed to resolve injectee for parameter " + parameter);
		}
	}

	@SuppressWarnings("serial")
	public static final class AbstractClassNotSupportedException extends BeanInjectionException {
		public AbstractClassNotSupportedException(final Class<?> clazz, final Throwable t) {
			super("Cannot instantiate " + clazz + " because it is abstract", t);
		}
	}

	@SuppressWarnings("serial")
	public static final class WrongNumberOfArgumentsException extends BeanInjectionException {
		public WrongNumberOfArgumentsException(final Constructor<?> constructor, final int required, final int provided) {
			super("Wrong number of arguments for " + constructor + ", required " + required + " vs provided " + provided);
		}
	}

	@SuppressWarnings("serial")
	public static final class BeanInstantiationException extends BeanInjectionException {
		public BeanInstantiationException(final Class<?> clazz, final Exception e) {
			super("Error when creating instance of " + clazz.getName(), e);
		}
	}

	@SuppressWarnings("serial")
	public static final class UnsatisfiedParameterInjectionException extends BeanInjectionException {
		public UnsatisfiedParameterInjectionException(final Parameter parameter, final UnsatisfiedResolutionException e) {
			super("Cannot resolve injectee for parameter " + parameter + " of " + parameter.getParameterizedType(), e);
		}
	}
}
