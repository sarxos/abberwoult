package com.github.sarxos.abberwoult.deployment.error;

import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.ASSISTED_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.INJECT_ANNOTATION;
import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.MESSAGE_HANDLER_ANNOTATION;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;


@SuppressWarnings("serial")
public class WrongAssistedArgumentsCountException extends IllegalStateException {

	public WrongAssistedArgumentsCountException(final MethodInfo handler, final ClassInfo recipientClass, final int assistedCount) {
		super(""
			+ "Methods annotated with both " + MESSAGE_HANDLER_ANNOTATION + " "
			+ "and " + INJECT_ANNOTATION + " must consume exactly one assisted "
			+ "argument, i.e. argument annotated with " + ASSISTED_ANNOTATION + " "
			+ "but found " + assistedCount + " assisted arguments on " + handler + " "
			+ "in " + recipientClass);
	}
}
