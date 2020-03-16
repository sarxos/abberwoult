package com.github.sarxos.abberwoult.deployment.error;

import org.jboss.jandex.DotName;

import com.github.sarxos.abberwoult.jandex.Reflector.ClassRef;


@SuppressWarnings("serial")
public class ImplementationMissingException extends IllegalStateException {
	public ImplementationMissingException(final ClassRef clazz, final DotName interf) {
		super("Class " + clazz + " should implement " + interf);
	}
}
