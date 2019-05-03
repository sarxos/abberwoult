package com.github.sarxos.abberwoult.deployment;

import static com.github.sarxos.abberwoult.util.CollectorUtils.toListWithSameSizeAs;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.nonOverridden;
import static com.github.sarxos.abberwoult.util.ReflectionUtils.unreflect;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.abberwoult.ActorCreator;
import com.github.sarxos.abberwoult.annotation.PreStart;
import com.github.sarxos.abberwoult.util.ReflectionUtils;

import io.vavr.control.Option;


public class PreStartRegistry {

	private static final Logger LOG = LoggerFactory.getLogger(PreStartRegistry.class);
	private static final Map<String, List<PreStartMethod>> RECORDS = new HashMap<>();
	private static final Predicate<Method> HAS_NO_ARGUMENTS = m -> m.getParameterCount() == 0;
	private static final Predicate<Method> IS_VOID = m -> m.getReturnType() == void.class;

	static void register(final Class<?> clazz) {

		if (clazz.isAnonymousClass() || Modifier.isAbstract(clazz.getModifiers())) {
			return;
		}

		final List<Method> methods = ReflectionUtils
			.getAnnotatedMethodsFromClass(clazz, PreStart.class, ActorCreator.STOP_CLASS)
			.stream()
			.filter(HAS_NO_ARGUMENTS)
			.filter(IS_VOID)
			.collect(toList());

		if (methods.isEmpty()) {
			return;
		}

		nonOverridden(methods);

		LOG.debug("Register {} pre start bindings for actor {}", methods.size(), clazz);

		RECORDS.computeIfAbsent(clazz.getName(), $ -> prepareEntry(methods));
	}

	private static List<PreStartMethod> prepareEntry(final List<Method> methods) {
		return unmodifiableList(methods
			.stream()
			.map(PreStartMethod::new)
			.collect(toListWithSameSizeAs(methods)));
	}

	public static List<PreStartMethod> getPreStartsFor(final Class<?> clazz) {
		return Option
			.of(RECORDS.get(clazz.getName()))
			.getOrElse(Collections::emptyList);
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
}
