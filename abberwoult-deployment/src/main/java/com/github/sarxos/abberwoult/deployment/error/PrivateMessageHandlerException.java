package com.github.sarxos.abberwoult.deployment.error;

import static com.github.sarxos.abberwoult.deployment.AbberwoultClasses.MESSAGE_HANDLER_ANNOTATION;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.MethodInfo;


@SuppressWarnings("serial")
public class PrivateMessageHandlerException extends IllegalStateException {

	public PrivateMessageHandlerException(final MethodInfo handler, final ClassInfo recipientClass) {
		super(""
			+ "Methods annotated with " + MESSAGE_HANDLER_ANNOTATION + " must not be "
			+ "private, but found private " + handler + " in " + recipientClass);
	}

}
