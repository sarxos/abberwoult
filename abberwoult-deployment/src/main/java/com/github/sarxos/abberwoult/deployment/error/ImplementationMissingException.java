package com.github.sarxos.abberwoult.deployment.error;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;


@SuppressWarnings("serial")
public class ImplementationMissingException extends IllegalStateException {
	public ImplementationMissingException(final ClassInfo clazz, final DotName interf) {
		super("Class " + clazz + " should implement " + interf);
	}
}
