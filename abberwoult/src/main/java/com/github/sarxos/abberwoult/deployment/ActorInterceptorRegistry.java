package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.CollectorUtils.optimizedList;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getAnnotatedParameterPosition;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.getObjectDistance;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.hasObservedParameters;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.hasValidableParameters;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.isAbstract;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.isInterface;
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

import com.github.sarxos.abberwoult.SimpleActor;
import com.github.sarxos.abberwoult.annotation.Event;
import com.github.sarxos.abberwoult.annotation.Receives;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import io.quarkus.runtime.annotations.Recorder;
import io.vavr.control.Option;


@Recorder
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
	 * Predicate to indicate if {@link Method} has any arguments (more than zero).
	 */
	private static final Predicate<Method> HAS_ARGUMENTS = m -> m.getParameterCount() > 0;

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
		Event.class);

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
		registerReceiversFrom(ReflectionUtils.getClazz(className));
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
}
