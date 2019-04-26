package com.github.sarxos.abberwoult.deployment.error;

import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.MESSAGE_HANDLER_ANNOTATION;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;


@SuppressWarnings("serial")
public class NoArgMessageHandlerException extends IllegalStateException {

	public NoArgMessageHandlerException(final MethodInfo handler, final ClassInfo recipient) {
		super(""
			+ "Methods annotated with " + MESSAGE_HANDLER_ANNOTATION + " must consume "
			+ "at least one parameter, but none found for " + handler + " in " + recipient);
	}

}
