package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getAnnotations;
import static com.github.sarxos.abberwoult.cdi.BeanUtils.getQualifiers;
import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.quarkus.arc.CurrentInjectionPointProvider.InjectionPointImpl;


@Singleton
public class BeanLocator {

	private static final int NO_POSITION = -1;

	private final BeanManager bm;
	private final Instance<Object> instance;

	@Inject
	public BeanLocator(final BeanManager bm) {
		this.bm = bm;
		this.instance = bm.createInstance();
	}

	public Object findBeanFor(final Constructor<?> constructor, final Parameter parameter, final int position) {
		final Type type = parameter.getParameterizedType();
		final Set<Annotation> annotations = asSet(getAnnotations(parameter));
		final Set<Annotation> qualifiers = asSet(getQualifiers(parameter));
		return findBean(type, qualifiers, annotations, constructor, position);
	}

	public Object findBeanFor(final Field field) {
		final Type type = field.getGenericType();
		final Set<Annotation> annotations = asSet(getAnnotations(field));
		final Set<Annotation> qualifiers = asSet(getQualifiers(field));
		return findBean(type, qualifiers, annotations, field, NO_POSITION);
	}

	private Object findBean(final Type type, final Set<Annotation> qualifiers, final Set<Annotation> annotations, final Member member, final int position) {
		final InjectionPoint ip = new InjectionPointImpl(type, type, qualifiers, null, annotations, member, position);
		final CreationalContext<Object> ctx = bm.createCreationalContext(null);
		return bm.getInjectableReference(ip, ctx);
	}

	public Object findBean(final Type type, final Annotation... qualifiers) {
		return instance.select((Class<?>) type, qualifiers).get();
	}

	private static Set<Annotation> asSet(Annotation[] array) {
		return new HashSet<>(asList(array));
	}
}
