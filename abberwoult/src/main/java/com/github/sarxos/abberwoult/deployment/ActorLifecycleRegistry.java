package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.isAbstract;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.isInterface;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.removeOverriddenFrom;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jboss.logging.Logger;

import com.github.sarxos.abberwoult.ActorCreator;
import com.github.sarxos.abberwoult.annotation.PostStop;
import com.github.sarxos.abberwoult.deployment.ActorInterceptorRegistry.PostStopMethod;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import io.quarkus.runtime.annotations.Recorder;
import io.vavr.control.Option;


/**
 * @author Bartosz Firyn (sarxos)
 */
@Recorder
public class ActorLifecycleRegistry {

	private static final Logger LOG = Logger.getLogger(ActorLifecycleRegistry.class);

	/**
	 * A static map where all {@link PostStop} annotation points are stored per class. This map is
	 * populated by a {@link ActorInterceptorRegistryTemplate} recorded in the compile time.
	 */
	private static final Map<String, List<PostStopMethod>> POSTSTOPS = new HashMap<>();

	/**
	 * Predicate to indicate if {@link Method} has no arguments.
	 */
	private static final Predicate<Method> HAS_NO_ARGUMENTS = m -> m.getParameterCount() == 0;

	/**
	 * Predicate to indicate if {@link Method} return type is void.
	 */
	private static final Predicate<Method> IS_VOID = m -> m.getReturnType() == void.class;

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
	 * Anonymous, abstract and interface classes are non-registrable.
	 *
	 * @param clazz
	 * @return
	 */
	public static boolean isNonRegistrable(final Class<?> clazz) {
		return clazz.isAnonymousClass() || isAbstract(clazz) || isInterface(clazz);
	}

	public void register(final String className) {

		final Class<?> clazz = ReflectionUtils.getClazz(className);

		registerPostStopsFrom(clazz);
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
}
