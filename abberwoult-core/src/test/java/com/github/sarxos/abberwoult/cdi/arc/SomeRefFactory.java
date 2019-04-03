package com.github.sarxos.abberwoult.cdi.arc;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class SomeRefFactory {

	private final SomeService service;
	private final SomeSystem system;

	@Inject
	public SomeRefFactory(SomeService service, SomeSystem system) {
		this.service = service;
		this.system = system;
	}

	@Produces
	@Dependent
	@SomeQualifier
	public SomeRef create(final InjectionPoint injection) {

		// injection.getAnnotated().isAnnotationPresent(SomeQualifier.class) => UOE
		// injection.getAnnotated().getAnnotation(SomeQualifier.class) => UOE
		// injection.getAnnotated().getAnnotations(SomeQualifier.class) => UOE
		// injection.getAnnotated().getAnnotations() => UOE
		// injection.getMember() => null
		
		return system.refOf(service.getName());
	}
}
