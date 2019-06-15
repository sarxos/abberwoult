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


/**
 * This class is used to find beans which matches injection point signature. The injection point here
 * is the {@link Constructor} or a {@link Field} reference. 
 *
 * @author Bartosz Firyn (sarxos)
 */
@Singleton
public class BeanLocator {

	private static final int NO_POSITION = -1;

	/**
	 * A {@link BeanManager} from CDI SPI.
	 */
	private final BeanManager bm;

	/**
	 * The {@link Instance} object from CDI SPI which is used to dynamically obtain instances of a beans
	 * with a specified combination of required type and qualifiers. 
	 */
	private final Instance<Object> instance;

	/**
	 * @param bm the {@link BeanManager} from CDI SPI
	 */
	@Inject
	public BeanLocator(final BeanManager bm) {
		this.bm = bm;
		this.instance = bm.createInstance();
	}

	/**
	 * Find bean to be injected into the given constructor, which is referenced by a given parameter present
	 * at given position.
	 *
	 * @param constructor the {@link Constructor} to inject bean into
	 * @param parameter the {@link Parameter} which references a bean
	 * @param position a parameter position
	 * @return Bean which matches {@link Parameter}
	 */
	public Object findBeanFor(final Constructor<?> constructor, final Parameter parameter, final int position) {
		final Type type = parameter.getParameterizedType();
		final Set<Annotation> annotations = asSet(getAnnotations(parameter));
		final Set<Annotation> qualifiers = asSet(getQualifiers(parameter));
		return findBean(type, qualifiers, annotations, constructor, position);
	}

	/**
	 * Find bean to be injected into a given {@link Field}.
	 *
	 * @param field the {@link Field} into which bean shall be injected
	 * @return Matching bean
	 */
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

	/**
	 * Find bean for a given type and given qualifiers.
	 *
	 * @param type the bean {@link Type}
	 * @param qualifiers the bean qualifiers
	 * @return Matching bean
	 */
	public Object findBean(final Type type, final Annotation... qualifiers) {
		return instance.select((Class<?>) type, qualifiers).get();
	}

	/**
	 * Converts {@link Annotation} array into a {@link Set}.
	 *
	 * @param array the {@link Annotation} array
	 * @return New {@link Set} which holds {@link Annotation} instances from given array
	 */
	private static Set<Annotation> asSet(Annotation[] array) {
		return new HashSet<>(asList(array));
	}
}
