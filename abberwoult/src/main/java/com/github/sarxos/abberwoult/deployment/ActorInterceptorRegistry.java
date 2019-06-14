package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.CollectorUtils.optimizedList;
import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getAnnotatedParameterPosition;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getObjectDistance;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.hasObservedParameters;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.hasValidableParameters;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.isAbstract;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.isInterface;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.removeOverriddenFrom;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.unreflect;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.inject.Inject;
import javax.validation.Valid;

import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.ActorCreator;
import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Observes;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import io.quarkus.runtime.annotations.Template;
import io.vavr.control.Option;


@Template
public class ActorInterceptorRegistry {

	private static final Logger LOG = Logger.getLogger(ActorInterceptorRegistry.class);

	/**
	 * A static map where all {@link Receives} annotation points are stored. This map is populated
	 * by a {@link ActorInterceptorRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<String, Map<String, MessageReceiverMethod>> RECEIVERS = new HashMap<>();

	/**
	 * List of observed events per class.
	 */
	private static final Map<String, List<Class<?>>> OBSERVED_EVENTS = new HashMap<>();

	/**
	 * A static map where all {@link PreStart} annotation points are stored per class. This map is
	 * populated by a {@link ActorInterceptorRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<String, List<PreStartMethod>> PRESTARTS = new HashMap<>();

	/**
	 * A static map where all {@link PostStop} annotation points are stored per class. This map is
	 * populated by a {@link ActorInterceptorRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<String, List<PostStopMethod>> POSTSTOPS = new HashMap<>();

	/**
	 * Predicate to indicate if {@link Method} has any arguments (more than zero).
	 */
	private static final Predicate<Method> HAS_ARGUMENTS = m -> m.getParameterCount() > 0;

	/**
	 * Predicate to indicate if {@link Method} has no arguments.
	 */
	private static final Predicate<Method> HAS_NO_ARGUMENTS = m -> m.getParameterCount() == 0;

	/**
	 * Predicate to indicate if {@link Method} return type is void.
	 */
	private static final Predicate<Method> IS_VOID = m -> m.getReturnType() == void.class;

	/**
	 * Comparator to sort {@link MessageReceiverMethod} by a distance between a message type and an
	 * {@link Object} class. The more specialized methods will be listed first.
	 */
	private static final Comparator<MessageReceiverMethod> BY_OBJECT_DISTANCE = (a, b) -> {
		final int distanceA = getObjectDistance(a.getMessageClass());
		final int distanceB = getObjectDistance(b.getMessageClass());
		return Integer.compare(distanceA, distanceB);
	};

	/**
	 * Where to stop inheritance tree scanning.
	 */
	private static final Class<?> STOP_CLASS = SimpleActor.class.getSuperclass();

	private static final Collection<Class<? extends Annotation>> MESSAGE_ANNOTATIONS = asList(
		Receives.class,
		Observes.class);

	/**
	 * Register given class as a {@link Receives} definer. Given class inheritance tree will be
	 * scanned and all methods which contains at least one parameter annotated with {@link Receives}
	 * annotations will be stored in internal handlers registry,
	 *
	 * @param clazz the class to scan
	 */
	static void registerReceiversFrom(final Class<?> clazz) {

		// allow concrete classes only

		if (isNonRegistrable(clazz)) {
			return;
		}

		final List<MessageReceiverMethod> methods = ReflectionUtils
			.getAnnotatedParameterMethodsFromClass(clazz, MESSAGE_ANNOTATIONS, STOP_CLASS)
			.stream()
			.filter(HAS_ARGUMENTS)
			.map(MessageReceiverMethod::new)
			.collect(toList());

		if (methods.isEmpty()) {
			return;
		} else {
			methods.sort(BY_OBJECT_DISTANCE);
		}

		LOG.debugf("Register %s message receivers for actor %s", methods.size(), clazz);

		final String classKey = clazz.getName();

		RECEIVERS.computeIfAbsent(classKey, $ -> prepareReceiversEntry(methods));

		OBSERVED_EVENTS.computeIfAbsent(classKey, $ -> prepareObservedEventsEntry(methods));
	}

	private static Map<String, MessageReceiverMethod> prepareReceiversEntry(final List<MessageReceiverMethod> methods) {

		final Map<String, MessageReceiverMethod> receivers = new LinkedHashMap<>();

		methods
			.stream()
			.peek(entry -> LOG.tracef("Register message receiver %s", entry.getName()))
			.forEach(entry -> receivers.putIfAbsent(entry.getMessageKey(), entry));

		return receivers;
	}

	private static List<Class<?>> prepareObservedEventsEntry(final List<MessageReceiverMethod> receivers) {

		final Set<Class<?>> observed = receivers
			.stream()
			.filter(MessageReceiverMethod::isObserved)
			.map(MessageReceiverMethod::getMessageClass)
			.peek(eventClass -> LOG.tracef("Register observed event type %s", eventClass))
			.collect(toSet());

		if (observed.isEmpty()) {
			return emptyList();
		}

		return unmodifiableList(optimizedList(observed));
	}

	public void register(final String className) {

		final Class<?> clazz = ReflectionUtils.getClazz(className);

		registerReceiversFrom(clazz);
		registerPreStartsFrom(clazz);
		registerPostStopsFrom(clazz);
	}

	/**
	 * Scan class methods and register all which were annotated with a {@link PreStart} annotation.
	 *
	 * @param clazz the class to scan
	 */
	static void registerPreStartsFrom(final Class<?> clazz) {

		final List<Method> methods = getNoArgVoidMethodsAnnotatedWith(clazz, PreStart.class);
		if (methods.isEmpty()) {
			return;
		}

		LOG.debugf("Register %s pre-start bindings for actor %s", methods.size(), clazz);

		PRESTARTS.computeIfAbsent(clazz.getName(), $ -> preparePreStartMethodEntry(methods));
	}

	/**
	 * Scan class methods and register all which were annotated with a {@link PostStop} annotation.
	 *
	 * @param clazz the class to scan
	 */
	static void registerPostStopsFrom(final Class<?> clazz) {

		final List<Method> methods = getNoArgVoidMethodsAnnotatedWith(clazz, PostStop.class);
		if (methods.isEmpty()) {
			return;
		}

		LOG.debugf("Register %s post-stop bindings for actor %s", methods.size(), clazz);

		POSTSTOPS.computeIfAbsent(clazz.getName(), $ -> preparePostStopMethodEntry(methods));
	}

	/**
	 * Scan class methods and collect these which a annotated with a given annotation, has not
	 * arguments and are void.
	 *
	 * @param clazz the class to scan
	 * @param annotation the annotation to search
	 * @return List of {@link Method} found in class
	 */
	private static List<Method> getNoArgVoidMethodsAnnotatedWith(final Class<?> clazz, final Class<? extends Annotation> annotation) {

		if (isNonRegistrable(clazz)) {
			return emptyList();
		}

		final List<Method> methods = ReflectionUtils
			.getAnnotatedMethodsFromClass(clazz, annotation, ActorCreator.STOP_CLASS)
			.stream()
			.filter(HAS_NO_ARGUMENTS)
			.filter(IS_VOID)
			.collect(toList());

		if (methods.isEmpty()) {
			return emptyList();
		} else {
			return removeOverriddenFrom(methods);
		}
	}

	/**
	 * Return a mapping between message class and corresponding handlers.
	 *
	 * @param clazz a declaring class
	 * @return A mapping between message class and corresponding handlers
	 */
	public static Option<Map<String, MessageReceiverMethod>> getReceiversFor(final Class<?> clazz) {
		return Option.of(RECEIVERS.get(clazz.getName()));
	}

	/**
	 * This method will return a {@link MessageReceiverMethod} for a given message class in a given
	 * declaring class.
	 *
	 * @param declaringClass a declaring class
	 * @param messageClass a message class
	 * @return A {@link MessageReceiverMethod} entry
	 */
	public static Option<MessageReceiverMethod> getReceiversFor(final Class<?> declaringClass, final Class<?> messageClass) {
		return getReceiversFor(declaringClass).map(mapping -> mapping.get(messageClass.getName()));
	}

	private static List<PreStartMethod> preparePreStartMethodEntry(final List<Method> methods) {
		return unmodifiableList(methods
			.stream()
			.map(PreStartMethod::new)
			.collect(toListWithSameSizeAs(methods)));
	}

	/**
	 * Get {@link PreStart} annotated methods from a registry cache.
	 *
	 * @param clazz the class, may be concrete actor class or interface
	 * @return Return {@link PreStartMethod} list for a given class.
	 */
	public static List<PreStartMethod> getPreStartsFor(final Class<?> clazz) {
		return Option
			.of(PRESTARTS.get(clazz.getName()))
			.getOrElse(Collections::emptyList);
	}

	private static List<PostStopMethod> preparePostStopMethodEntry(final List<Method> methods) {
		return unmodifiableList(methods
			.stream()
			.map(PostStopMethod::new)
			.collect(toListWithSameSizeAs(methods)));
	}

	/**
	 * Get {@link PostStop} annotated methods from a registry cache.
	 *
	 * @param clazz the class, may be concrete actor class or interface
	 * @return Return {@link PostStopMethod} list for a given class.
	 */
	public static List<PostStopMethod> getPostStopsFor(final Class<?> clazz) {
		return Option
			.of(POSTSTOPS.get(clazz.getName()))
			.getOrElse(Collections::emptyList);
	}

	public static List<Class<?>> getObservedEventsFor(final Class<?> clazz) {
		return Option
			.of(OBSERVED_EVENTS.get(clazz.getName()))
			.getOrElse(Collections::emptyList);
	}

	/**
	 * Anonymous, abstract and interface classes are non-registrable.
	 *
	 * @param clazz
	 * @return
	 */
	public static boolean isNonRegistrable(final Class<?> clazz) {
		return clazz.isAnonymousClass() || isAbstract(clazz) || isInterface(clazz);
	}

	public static class MessageReceiverMethod {

		private final Method method;
		private final Class<?> messageClass;
		private final int messageParameterPosition;
		private final MethodHandle handle;
		private final boolean validable;
		private final boolean observed;
		private final boolean injectable;

		public MessageReceiverMethod(final Method method) {
			this.method = method;
			this.messageClass = method.getParameterTypes()[0];
			this.messageParameterPosition = getAnnotatedParameterPosition(method, Receives.class);
			this.handle = unreflect(getDeclaringClass(), method);
			this.validable = hasValidableParameters(method);
			this.observed = hasObservedParameters(method);
			this.injectable = method.isAnnotationPresent(Inject.class);
		}

		public Class<?> getDeclaringClass() {
			return method.getDeclaringClass();
		}

		public Class<?> getMessageClass() {
			return messageClass;
		}

		public int getMessageParameterPosition() {
			return messageParameterPosition;
		}

		public MethodHandle getHandle() {
			return handle;
		}

		/**
		 * @return True if received parameter was annotated with {@link Valid}, false otherwise
		 */
		public boolean isValidable() {
			return validable;
		}

		public boolean isObserved() {
			return observed;
		}

		/**
		 * @return True if method was annotated with {@link Inject}, false otherwise
		 */
		public boolean isInjectable() {
			return injectable;
		}

		public String getMessageKey() {
			return getMessageClass().getName();
		}

		/**
		 * @return Interned method name
		 */
		public String getName() {
			return method.getName();
		}

		public Class<?> getReturnedType() {
			return method.getReturnType();
		}
	}

	public static class PreStartMethod {

		private final Method method;
		private final MethodHandle handle;

		public PreStartMethod(final Method method) {
			this.method = method;
			this.handle = unreflect(getDeclaringClass(), method);
		}

		public Class<?> getDeclaringClass() {
			return method.getDeclaringClass();
		}

		public MethodHandle getHandle() {
			return handle;
		}

		/**
		 * @return Interned method name
		 */
		public String getName() {
			return method.getName();
		}

		public Class<?> getReturnedType() {
			return method.getReturnType();
		}
	}

	public static class PostStopMethod {

		private final Method method;
		private final MethodHandle handle;

		public PostStopMethod(final Method method) {
			this.method = method;
			this.handle = unreflect(getDeclaringClass(), method);
		}

		public Class<?> getDeclaringClass() {
			return method.getDeclaringClass();
		}

		public MethodHandle getHandle() {
			return handle;
		}

		/**
		 * @return Interned method name
		 */
		public String getName() {
			return method.getName();
		}

		public Class<?> getReturnedType() {
			return method.getReturnType();
		}
	}
}
