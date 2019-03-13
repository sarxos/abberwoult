package com.github.sarxos.abberwoult;

import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static org.apache.commons.lang3.reflect.TypeUtils.isAssignable;

import akka.actor.Actor;
import akka.actor.Props;
import akka.japi.Creator;
import com.github.sarxos.abberwoult.util.CollectorUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.glassfish.hk2.api.ServiceLocator;


/**
 * This is actor creator which crafts wired actor instances.
 *
 * @author Bartosz Firyn (sarxos)
 */
public class ActorCreator implements Creator<Actor> {

	/**
	 * Serial.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Primitive type to boxed type mapping.
	 */
	private static final Map<Type, Type> BOXED = new HashMap<>();

	// put primitives into mapping

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
	 * {@link ServiceLocator} used to perform dependency injection.
	 */
	private final ServiceLocator locator;

	/**
	 * Actor class.
	 */
	private final Class<? extends Actor> clazz;

	/**
	 * Additional, but still optional, actor constructor arguments.
	 */
	private final Object[] args;

	/**
	 * Creates new {@link ActorCreator} instance with given {@link ServiceLocator} and class which
	 * describes the actor to be created. Constructor takes also vararg objects list which are the
	 * arguments (optional) to be passed down to the actor constructor. Please note that in case
	 * when actor constructor is annotated with {@link Inject}, the arguments list will be ignored
	 * (creator will try to resolve all necessary arguments using dependency injection support and
	 * original arguments are ignored).
	 *
	 * @param locator the service locator to be used to wire created instance
	 * @param clazz the actor class which should be created
	 * @param args the actor constructor arguments (optional, ignored if {@link Inject} used)
	 */
	public ActorCreator(final ServiceLocator locator, final Class<? extends Actor> clazz, final Object... args) {
		this.locator = locator;
		this.clazz = clazz;
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
	 * @throws NoSuitableActorConstructorException if no suitable constructor has been found
	 */
	private static Constructor<?> findMatchingConstructor(final Class<?> clazz, final Object[] args) {

		final Constructor<?>[] constructors = clazz.getDeclaredConstructors();

		// in case if there is only one constructor available in a given class, there is no
		// ambiguity - just use it

		if (constructors.length == 1) {
			return constructors[0];
		}

		// we need to find appropriate one in case there are more then one constructor, this can be
		// done by checking types of arguments array vs constructor types expected in the
		// constructor input

		ctors: for (final Constructor<?> constructor : constructors) {

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
			for (final Constructor<?> constructor : constructors) {
				if (constructor.isAnnotationPresent(Inject.class)) {
					return constructor;
				}
			}
		}

		// throw exception if no suitable constructor has been found

		throw new NoSuitableActorConstructorException(clazz, constructors, args);
	}

	@Override
	public Actor create() throws Exception {

		// find constructors (we expect only one)

		final Constructor<?> constructor = findMatchingConstructor(clazz, args);

		// use hk2 to instantiate actor object (it won't be managed by hk2, though) if
		// constructor should be supplied with injectable arguments, i.e. when Inject
		// is used on actor constructor, or use standard reflection-based instantiation
		// with predefined arguments in case when there is no Inject annotation present

		final Actor actor;

		if (constructor.isAnnotationPresent(Inject.class)) {
			actor = locator.create(clazz);
		} else {
			actor = instantiate(constructor, args);
		}

		// instantiate, wire and count actor

		locator.inject(actor);
		locator.postConstruct(actor);

		return actor;
	}

	private Actor instantiate(final Constructor<?> constructor, final Object[] args) {

		// set correct accessibility flag if not set

		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);
		}

		// instantiate

		try {
			return (Actor) constructor.newInstance(args);
		} catch (InstantiationException ie) {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				throw new IllegalArgumentException(""
					+ "Cannot instantiate actor because " + clazz + " is abstract. "
					+ "Please make sure to use concrete type instead of abstract one.", ie);
			} else {
				throw new IllegalStateException(""
					+ "Instantiation exception when creating object of " + clazz + " "
					+ "with " + args.length + " arguments", ie);
			}
		} catch (Exception e) {
			throw new IllegalStateException("Cannot create " + clazz + " instance with " + args.length + " arguments", e);
		}
	}

	public ServiceLocator getLocator() {
		return locator;
	}

	public Class<? extends Actor> getClazz() {
		return clazz;
	}

	public Object[] getArgs() {
		return args;
	}

	public static abstract class ActorCreatorException extends IllegalStateException {

		/**
		 * Serial.
		 */
		private static final long serialVersionUID = 1L;
	}

	/**
	 * This exception is being thrown when {@link ActorCreator} cannot find suitable constructor to
	 * create actor with the arguments given by {@link Props} object.
	 *
	 * @author Bartosz Firyn (sarxos)
	 */
	public static final class NoSuitableActorConstructorException extends ActorCreatorException {

		/**
		 * Serial.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * The actor class.
		 */
		private final Class<?> clazz;

		/**
		 * All of actor constructors.
		 */
		private final Collection<Constructor<?>> constructors;

		/**
		 * The arguments from {@link Props}.
		 */
		private final Collection<Object> arguments;

		/**
		 * @param clazz the actor class
		 * @param constructors the actor constructors
		 * @param arguments the arguments from {@link Props}
		 */
		public NoSuitableActorConstructorException(final Class<?> clazz, final Constructor<?>[] constructors, final Object[] arguments) {
			this.clazz = clazz;
			this.constructors = Arrays.asList(constructors);
			this.arguments = CollectorUtils.stream(arguments)
				.map(Object::getClass)
				.collect(toListWithSameSizeAs(arguments));
		}

		@Override
		public String getMessage() {
			return MessageFormat.format(""
				+ "No suitable constructor found to create {0} instance, "
				+ "candidates are {1}, but arguments are {2}",
				clazz, constructors, arguments);
		}
	}
}
