package com.github.sarxos.abberwoult.cdi;

import static com.github.sarxos.abberwoult.cdi.BeanUtils.getQualifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class BeanLocator {

	private final Instance<Object> instance;

	@Inject
	public BeanLocator(final BeanManager bm) {
		this.instance = bm.createInstance();
	}

	public Object findBeanFor(final Parameter parameter) {
		return findBean(parameter.getParameterizedType(), getQualifiers(parameter));
	}

	public Object findBeanFor(final Field field) {
		return findBean(field.getGenericType(), getQualifiers(field));
	}

	public Object findBean(final Type type, final Annotation[] qualifiers) {
		return instance.select((Class<?>) type, qualifiers).get();
	}
}
